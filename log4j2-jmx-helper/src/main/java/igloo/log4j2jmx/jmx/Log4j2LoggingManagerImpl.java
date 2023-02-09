package igloo.log4j2jmx.jmx;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.logging.log4j.core.Logger;

import igloo.julhelper.api.JulLoggingManager;
import igloo.log4j2jmx.jmx.Log4j2LoggingConfigurator.LevelWrapper;

public class Log4j2LoggingManagerImpl implements Log4j2LoggingManager {

	private final Optional<JulLoggingManager> julLoggingManager;

	private final Log4j2LoggingConfigurator log4j2Logging;

	private final Set<Logger> loggers = ConcurrentHashMap.newKeySet();

	private final Map<String, LevelWrapper> originalLevels = new ConcurrentHashMap<>();

	public Log4j2LoggingManagerImpl() {
		this(null);
	}

	public Log4j2LoggingManagerImpl(JulLoggingManager julLoggingManager) {
		this(julLoggingManager, new Log4j2LoggingConfigurator());
	}

	public Log4j2LoggingManagerImpl(JulLoggingManager julLoggingManager, Log4j2LoggingConfigurator log4j2Logging) {
		super();
		this.julLoggingManager = Optional.ofNullable(julLoggingManager);
		this.log4j2Logging = log4j2Logging;
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
		log4j2Logging.doSetLevel(name, level, loggers, originalLevels);
		julLoggingManager.ifPresent(m -> m.setLevelIfWellKnown(name, level));
	}

	/**
	 * @see Log4j2LoggingManager#unsetLevel(String)
	 */
	@Override
	public synchronized void unsetLevel(final String name) {
		log4j2Logging.doUnsetLevel(name, loggers, originalLevels);
		julLoggingManager.ifPresent(m -> m.unsetLevel(name));
	}

	/**
	 * @see Log4j2LoggingManager#reset()
	 */
	@Override
	public synchronized void reset() {
		Set<Logger> loggersCopy = new HashSet<>(this.loggers);
		for (Logger logger : loggersCopy) {
			log4j2Logging.doUnsetLevel(logger.getName(), loggers, originalLevels);
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

	// Used for unit tests
	public Set<Logger> getLoggers() {
		return loggers;
	}

}
