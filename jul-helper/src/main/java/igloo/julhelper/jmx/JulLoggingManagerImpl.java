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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import igloo.julhelper.api.JulLoggingManager;

/**
 * Implementation for {@link JulLoggingManager} MBean. All public methods are synchronized to ensure
 * {@link #loggers} consistency.
 */
public class JulLoggingManagerImpl implements JulLoggingManager {

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

	private final JulLoggingConfigurator julLoggingConfigurator;

	public JulLoggingManagerImpl(String julKnownLoggersResourcePath) {
		this(julKnownLoggersResourcePath, new JulLoggingConfigurator());
	}

	/**
	 * `julKnownLoggersResourcePath` is a resource path for a file containing well-known JUL logger names.
	 * It is used to conditionally apply modifications from {@link #setLevelIfWellKnown(String, String)} calls.
	 * `setLevel` is done only for logger names among well-known names.
	 * 
	 * @param julKnownLoggersResourcePath a resource path to load well-known JUL logger names. The resource must exists.
	 *        Use null to skip well-known logger names loading.
	 * @param julLoggingConfigurator {@link JulLoggingConfigurator} instance; used to perform JUL configuration.
	 */
	public JulLoggingManagerImpl(String julKnownLoggersResourcePath, JulLoggingConfigurator julLoggingConfigurator) {
		super();
		this.julLoggingConfigurator = julLoggingConfigurator;
		
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
			julLoggingConfigurator.updateJulKnownLoggers(julKnownLoggersResourcePath, supplier, false, julKnownLoggers);
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
		julLoggingConfigurator.setLevel(name, level, loggers, julLevelMapping);
	}

	/**
	 * @see JulLoggingManager#setLevelIfWellKnown(String, String)
	 */
	@Override
	public synchronized void setLevelIfWellKnown(String name, String level) {
		if (julLoggingConfigurator.matchJulKnownLoggers(name, julKnownLoggers)) {
			setLevel(name, level);
		}
	}

	/**
	 * @see JulLoggingManager#unsetLevel(String)
	 */
	@Override
	public synchronized void unsetLevel(final String name) {
		julLoggingConfigurator.doUnsetLevel(name, loggers);
	}

	/**
	 * @see JulLoggingManager#reset()
	 */
	@Override
	public synchronized void reset() {
		Set<Logger> loggersCopy = new HashSet<>(this.loggers);
		for (Logger logger : loggersCopy) {
			julLoggingConfigurator.doUnsetLevel(logger.getName(), loggers);
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
		Supplier<Scanner> supplier = () -> new Scanner(julKnownLoggers);
		julLoggingConfigurator.updateJulKnownLoggers("JMX addJulKnownLoggers operation", supplier, false, this.julKnownLoggers);
	}

	/**
	 * @see JulLoggingManager#updateJulKnownLoggers(String)
	 */
	@Override
	public void updateJulKnownLoggers(String julKnownLoggers) {
		Supplier<Scanner> supplier = () ->  new Scanner(julKnownLoggers);
		julLoggingConfigurator.updateJulKnownLoggers("JMX addJulKnownLoggers operation", supplier, true, this.julKnownLoggers);
	}

	// Used for unit tests
	public Set<Logger> getLoggers() {
		return loggers;
	}

}
