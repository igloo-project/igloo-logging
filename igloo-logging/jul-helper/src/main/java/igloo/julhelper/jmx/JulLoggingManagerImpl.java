package igloo.julhelper.jmx;

import java.lang.ref.WeakReference;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import igloo.julhelper.api.JulLoggingManager;

/**
 * Implementation for {@link JulLoggingManager} MBean. All public methods are synchronized to ensure
 * {@link #loggers} consistency.
 */
public class JulLoggingManagerImpl implements JulLoggingManager {

	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(JulLoggingManagerImpl.class);

	/**
	 * A set of well-known JUL logger names. Used to conditionally apply  {@link #setLevelIfWellKnown(String, String)}
	 * calls.
	 */
	private final Set<String> julKnownLoggers = new TreeSet<>();

	/**
	 * A mapping to transform level given to {@link #setLevel(String, String)} when it is not known by JUL.
	 */
	private final Map<String, Level> julLevelMapping = new HashMap<>();

	/**
	 * This set is used to store managed loggers. It is needed:
	 * 
	 * * to keep track of modified loggers, provides feedback, and perform reset operations;
	 * * to ensure that managed loggers are not garbage collected (if not used, logger are stored as
	 *   {@link WeakReference}.
	 */
	private final Set<Logger> loggers = ConcurrentHashMap.newKeySet();

	public JulLoggingManagerImpl() {
		this(null);
	}

	/**
	 * `julKnownLoggersResourcePath` is a resource path for a file containing well-known JUL logger names.
	 * It is used to conditionally apply modifications from {@link #setLevelIfWellKnown(String, String)} calls.
	 * `setLevel` is done only for logger names among well-known names.
	 * 
	 * @param julKnownLoggersResourcePath a resource path to load well-known JUL logger names. The resource must exists.
	 *        Use null to skip well-known logger names loading.
	 */
	public JulLoggingManagerImpl(String julKnownLoggersResourcePath) {
		super();
		julLevelMapping.put("ALL", Level.FINEST);
		julLevelMapping.put("TRACE", Level.FINEST);
		julLevelMapping.put("DEBUG", Level.FINE);
		julLevelMapping.put("INFO", Level.INFO);
		julLevelMapping.put("WARN", Level.WARNING);
		julLevelMapping.put("ERROR", Level.SEVERE);
		julLevelMapping.put("OFF", Level.OFF);
		// resource path
		// empty or blank -> default value
		// "default" -> default value
		// null -> absent
		// other values -> used as a resource path
		String defaultResourcePath = "jul-helper/well-known-jul-loggers.txt";
		Optional<String> resourceOptional = Optional.ofNullable(julKnownLoggersResourcePath)
				.map(s -> s.isBlank() ? defaultResourcePath : s)
				.map(s -> "default".equalsIgnoreCase(s) ? defaultResourcePath : s);
		if (resourceOptional.isPresent()) {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			Supplier<Scanner> supplier = () -> new Scanner(classLoader.getResourceAsStream(resourceOptional.get()), StandardCharsets.UTF_8);
			updateJulKnownLoggers(julKnownLoggersResourcePath, supplier, false);
		}
	}

	/**
	 * @see JulLoggingManager#getLoggerNames
	 */
	@Override
	public synchronized List<String> getLoggerNames() {
		List<String> names = loggers.stream().map(Logger::getName).collect(Collectors.toList());
		names.sort(String::compareTo);
		return names;
	}

	/**
	 * @see JulLoggingManager#getLoggerConfig
	 */
	@Override
	public synchronized String getLoggerConfig() {
		List<Logger> names = loggers.stream().collect(Collectors.toList());
		names.sort((l1, l2) -> l1.getName().compareTo(l2.getName()));
		return names.stream().map(l -> String.format("%s=%s", l.getName(), l.getLevel().getName())).collect(Collectors.joining("\n"));
	}

	/**
	 * @see JulLoggingManager#setLevel(String, String)
	 */
	@Override
	public synchronized void setLevel(final String name, final String level) {
		Logger logger = getLogger(name);
		logger.setLevel(parseLevel(level));
		clearHandlers(logger);
		logger.setUseParentHandlers(false);
		logger.addHandler(new SLF4JBridgeHandler());
		loggers.add(logger);
	}

	/**
	 * @see JulLoggingManager#setLevelIfWellKnown(String, String)
	 */
	@Override
	public synchronized void setLevelIfWellKnown(String name, String level) {
		if (matchJulKnownLoggers(name)) {
			setLevel(name, level);
		}
	}

	/**
	 * @see JulLoggingManager#unsetLevel(String)
	 */
	@Override
	public synchronized void unsetLevel(final String name) {
		doUnsetLevel(name);
	}

	/**
	 * @see JulLoggingManager#reset()
	 */
	@Override
	public synchronized void reset() {
		Set<Logger> loggersCopy = new HashSet<>(this.loggers);
		for (Logger logger : loggersCopy) {
			doUnsetLevel(logger.getName());
		}
	}

	/**
	 * @see JulLoggingManager#getJulKnownLoggers()
	 */
	@Override
	public Set<String> getJulKnownLoggers() {
		return julKnownLoggers;
	}

	/**
	 * @see JulLoggingManager#addJulKnownLoggers(String)
	 */
	@Override
	public void addJulKnownLoggers(String julKnownLoggers) {
		Supplier<Scanner> supplier = () ->  new Scanner(julKnownLoggers);
		updateJulKnownLoggers("JMX addJulKnownLoggers operation", supplier, false);
	}

	/**
	 * @see JulLoggingManager#updateJulKnownLoggers(String)
	 */
	@Override
	public void updateJulKnownLoggers(String julKnownLoggers) {
		Supplier<Scanner> supplier = () ->  new Scanner(julKnownLoggers);
		updateJulKnownLoggers("JMX addJulKnownLoggers operation", supplier, true);
	}

	private Level parseLevel(final String level) {
		try {
			return Level.parse(level);
		} catch (IllegalArgumentException e) {
			if (julLevelMapping.containsKey(level.toUpperCase())) {
				return julLevelMapping.get(level.toUpperCase());
			} else {
				throw new RuntimeException(String.format("%s cannot be mapped to a JUL level", level));
			}
		}
	}

	/**
	 * Compare logger name against known JUL loggers to decide if change must be propagated to JUL.
	 */
	private boolean matchJulKnownLoggers(String loggerName) {
		return julKnownLoggers.stream().anyMatch(loggerName::startsWith);
	}

	private void updateJulKnownLoggers(String updateSource, Supplier<Scanner> supplier, boolean reset) {
		if (reset) {
			LOGGER.info("Removing all JUL known loggers ({} items removed)", julKnownLoggers.size());
			julKnownLoggers.clear();
		}
		LOGGER.info("Loading JUL known loggers from resource {}.", updateSource);
		int i = 0;
		try (Scanner scanner = supplier.get()) {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if (!line.startsWith("#")) {
					// remove trailing newline and any other leading and trailing whitespaces
					String strippedLine = line.strip();
					LOGGER.trace("Adding JUL known logger {}", strippedLine);
					julKnownLoggers.add(strippedLine);
					i++;
				}
			}
		}
		LOGGER.info("Loaded {} JUL known loggers.", i);
	}

	/**
	 * Reset a managed logger state : handlers are removed, parent handlers usage is enabled, and level set to null.
	 * Logger is removed from the list of managed loggers.
	 * 
	 * @param name a logger name. Required.
	 */
	private void doUnsetLevel(final String name) {
		Logger logger = getLogger(name);
		clearHandlers(logger);
		logger.setUseParentHandlers(true);
		logger.setLevel(null);
		loggers.remove(logger);
	}

	/**
	 * Remove all logger handlers.
	 * 
	 * @param name a logger name. Required.
	 */
	private void clearHandlers(Logger logger) {
		for (Handler handler : logger.getHandlers()) {
			logger.removeHandler(handler);
		}
	}

	/**
	 * Retrieve a logger from list of managed loggers, else retrieve it from JUL API.
	 * 
	 * @param name a logger name. Required.
	 */
	private Logger getLogger(final String name) {
		return loggers.stream().filter(i -> i.getName().equals(name))
				.findFirst()
				.orElseGet(() -> Logger.getLogger(name));
	}

}
