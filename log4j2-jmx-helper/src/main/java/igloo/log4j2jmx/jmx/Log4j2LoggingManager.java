package igloo.log4j2jmx.jmx;

import java.util.List;
import java.util.Set;

import igloo.julhelper.api.JulLoggingManager;

/**
 * This MBean is a complement to log4j2 JMX implementation. It allows to update logger level for loggers not
 * configured (in the default implementation, only configured loggers can be modified).
 * 
 * Attributes and operations allow to update arbitrary log level, to show current managed loggers, and reset
 * loggers state by restoring original level.
 */
public interface Log4j2LoggingManager {

	/**
	 * List currently configured logger names. Only loggers managed by {@link Log4j2LoggingManager} are listed.
	 * 
	 * @return managed logger names.
	 */
	List<String> getLoggerNames();

	/**
	 * Switch the logger `name` to `level` and reload Log4j2 loggers.
	 * 
	 * @param name a logger name. Required.
	 * @param level a Log4j2 logging level (TRACE, DEBUG, INFO, WARN, INDO). Required.
	 */
	void setLevel(String name, String level);

	/**
	 * Reset logger `name` level, reset handlers and enable usage of parent handlers.
	 * 
	 * @param name a logger name. Required.
	 */
	void unsetLevel(String name);

	/**
	 * Print managed configuration as a string.
	 * 
	 * <pre>{@code
	 * logger.name=FINE (original: INFO/NONE)
	 * logger2.name=INFO (original: WARN/NONE)
	 * ...
	 * }</pre>
	 * 
	 * @return Overriden configuration as a string.
	 */
	String getLoggerConfig();

	/**
	 * Reset all managed loggers.
	 */
	void reset();

	/**
	 * @return list of JUL known loggers.
	 * 
	 * @see JulLoggingManager#getJulKnownLoggers()
	 */
	Set<String> getJulKnownLoggers();

	/**
	 * Add a set of JUL known loggers names;
	 * 
	 * @param julKnownLoggers a list of logger names, separated by any whitespace or newline character.
	 */
	void addJulKnownLoggers(String julKnownLoggers);

	/**
	 * Reset JUL known loggers list with provided values. This may lead to inconsistency if there is currently
	 * modified loggers matching previous or new logger's names list.
	 * 
	 * @param julKnownLoggers a list of logger names, separated by any whitespace or newline character.
	 */
	void updateJulKnownLoggers(String julKnownLoggers);

	/**
	 * Is {@link JulLoggingManager} updates enabled ? Only changes to logger configured as JUL known loggers are
	 * propagated.
	 * 
	 * @return true if set/unset level events are sent to {@link JulLoggingManager}
	 */
	boolean getJulLoggingManagementEnabled();

}