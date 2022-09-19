package igloo.log4j2jmx.jmx;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

import igloo.julhelper.api.JulLoggingManager;

public class Log4j2LoggingManagerImpl implements Log4j2LoggingManager {

	private final Optional<JulLoggingManager> julLoggingManager;

	private final Set<Logger> loggers = ConcurrentHashMap.newKeySet();

	private final Map<String, LevelWrapper> originalLevels = new ConcurrentHashMap<>();

	public Log4j2LoggingManagerImpl() {
		this(null);
	}

	public Log4j2LoggingManagerImpl(JulLoggingManager julLoggingManager) {
		super();
		this.julLoggingManager = Optional.ofNullable(julLoggingManager);
	}

	/**
	 * @see Log4j2LoggingManager#getLoggerNames()
	 */
	@Override
	public synchronized List<String> getLoggerNames() {
		List<String> names = loggers.stream().map(Logger::getName).collect(Collectors.toList());
		names.sort(String::compareTo);
		return names;
	}

	/**
	 * @see Log4j2LoggingManager#getLoggerConfig()
	 */
	@Override
	public synchronized String getLoggerConfig() {
		List<Logger> sortedLoggers = loggers.stream().collect(Collectors.toList());
		sortedLoggers.sort((l1, l2) -> l1.getName().compareTo(l2.getName()));
		return sortedLoggers.stream()
				.map(l -> String.format(
						"%s=%s (original: %s)",
						// logger name
						l.getName(),
						// = current level
						l.getLevel().name(),
						// (original: <original level>)
						Optional.ofNullable(originalLevels.get(l.getName())).map(LevelWrapper::name).orElse("NONE")
				))
				.collect(Collectors.joining("\n"));
	}

	/**
	 * @see Log4j2LoggingManager#setLevel(String, String)
	 */
	@Override
	public synchronized void setLevel(final String name, final String level) {
		Logger logger = doSetLevel(name, level);
		Level log4jLevel = Level.valueOf(level);
		logger.setLevel(log4jLevel);
		((LoggerContext) LogManager.getContext(false)).updateLoggers();
		julLoggingManager.ifPresent(m -> m.setLevelIfWellKnown(name, level));
	}

	/**
	 * @see Log4j2LoggingManager#unsetLevel(String)
	 */
	@Override
	public synchronized void unsetLevel(final String name) {
		doUnsetLevel(name);
		julLoggingManager.ifPresent(m -> m.unsetLevel(name));
	}

	/**
	 * @see Log4j2LoggingManager#reset()
	 */
	@Override
	public synchronized void reset() {
		Set<Logger> loggersCopy = new HashSet<>(this.loggers);
		for (Logger logger : loggersCopy) {
			doUnsetLevel(logger.getName());
			julLoggingManager.ifPresent(m -> m.unsetLevel(logger.getName()));
		}
	}

	/**
	 * @see Log4j2LoggingManager#getJulKnownLoggers()
	 */
	@Override
	public Set<String> getJulKnownLoggers() {
		return julLoggingManager.map(JulLoggingManager::getJulKnownLoggers).orElseGet(Collections::emptySet);
	}

	/**
	 * @see Log4j2LoggingManager#addJulKnownLoggers(String)
	 */
	@Override
	public void addJulKnownLoggers(String julKnownLoggers) {
		julLoggingManager.ifPresent(m -> m.addJulKnownLoggers(julKnownLoggers));
	}

	/**
	 * @see Log4j2LoggingManager#updateJulKnownLoggers(String)
	 */
	@Override
	public void updateJulKnownLoggers(String julKnownLoggers) {
		julLoggingManager.ifPresent(m -> m.updateJulKnownLoggers(julKnownLoggers));
	}

	/**
	 * @see Log4j2LoggingManager#getJulLoggingManagementEnabled()
	 */
	@Override
	public boolean getJulLoggingManagementEnabled() {
		return julLoggingManager.isPresent();
	}

	private Logger doSetLevel(final String name, final String levelAsString) {
		Logger logger = getLogger(name);
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		Configuration conf = ctx.getConfiguration();
		LoggerConfig lconf = conf.getLoggerConfig(name);
		Level originalLevel = lconf.getLevel();
		if (levelAsString != null) {
			if (!lconf.getName().equals(logger.getName())) {
				lconf = new LoggerConfig(logger.getName(), Level.valueOf(levelAsString), true);
				conf.addLogger(logger.getName(), lconf);
			} else {
				lconf.setLevel(Level.valueOf(levelAsString));
			}
			originalLevels.computeIfAbsent(name, (n) -> new LevelWrapper(originalLevel));
		} else {
			Level targetLevel = popOriginalLevel(name);
			if (targetLevel == null) {
				conf.removeLogger(name);
			} else {
				lconf.setLevel(targetLevel);
			}
		}
		
		ctx.updateLoggers(conf);
		
		if (levelAsString == null) {
			loggers.remove(logger);
		} else {
			loggers.add(logger);
		}
		return logger;
	}

	private Level popOriginalLevel(final String name) {
		if (originalLevels.containsKey(name)) {
			return originalLevels.remove(name).level;
		} else {
			return null;
		}
	}

	private void doUnsetLevel(final String name) {
		doSetLevel(name, null);
	}

	private Logger getLogger(final String name) {
		return loggers.stream().filter(i -> i.getName().equals(name))
				.findFirst()
				.orElseGet(() -> (Logger) LogManager.getLogger(name));
	}

	/**
	 * This class allows to store null {@link Level} values in {@link Log4j2LoggingManagerImpl#loggers}.
	 */
	private static class LevelWrapper {
		private final Level level;
		
		private LevelWrapper(Level level) {
			this.level = level;
		}
		
		private String name() {
			return this.level != null ? this.level.name() : "NONE";
		}
	}

}
