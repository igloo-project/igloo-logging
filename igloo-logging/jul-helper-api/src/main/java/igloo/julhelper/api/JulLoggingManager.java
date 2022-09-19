package igloo.julhelper.api;

import java.util.List;
import java.util.Set;

/**
 * Attributes and operations provided by this MBean allow to reconfigure at runtime {@link SLF4JBridgeHandler}
 * and logger levels.
 * 
 * To enable a logger, use {@link JulLoggingManager#setLevel(String, String)} operation, and enable the corresponding
 * logger in SLF4J backend.
 * 
 * To restore the original configuration, use either {@link #unsetLevel(String name)} to restore one logger or
 * {@link #reset()} to reset all loggers configuration.
 * 
 * This helper expects that all loggers are initialized with a null level (behavior is delegated to parent).
 */
public interface JulLoggingManager {

	/**
	 * List currently configured logger names. Only loggers managed by {@link JulLoggingManager} are listed.
	 * 
	 * @return managed logger names.
	 */
	List<String> getLoggerNames();

	/**
	 * Switch the logger `name` to `level`, add {@link SLF4JBridgeHandler} and disable parent handlers.
	 * 
	 * @param name a logger name. Required.
	 * @param level a JUL logging level (FINEST, FINER, FINE, CONFIG, INFO, WARNING, SEVERE, OFF). Only FINEST, FINE,
	 *              INFO and WARNING levels are bound to SLF4J levels. Required.
	 * @see SLF4JBridgeHandler
	 */
	void setLevel(String name, String level);

	/**
	 * Switch the logger `name` to `level`, add {@link SLF4JBridgeHandler} and disable parent handlers if logger
	 * is a JUL known logger (see {@link #getJulKnownLoggers()}.
	 * 
	 * @param name a logger name. Required.
	 * @param level a JUL logging level (FINEST, FINER, FINE, CONFIG, INFO, WARNING, SEVERE, OFF). Only FINEST, FINE,
	 *              INFO and WARNING levels are bound to SLF4J levels. Required.
	 * @see SLF4JBridgeHandler
	 */
	void setLevelIfWellKnown(String name, String level);

	/**
	 * Reset logger `name` level, reset handlers and enable usage of parent handlers.
	 * 
	 * @param name a logger name. Required.
	 */
	void unsetLevel(String name);

	/**
	 * Print managed configuration as a string.
	 * 
	 * ```ini
	 * logger.name=FINE
	 * logger2.name=INFO
	 * ```
	 * 
	 * @return
	 */
	String getLoggerConfig();

	/**
	 * Reset all managed loggers.
	 */
	void reset();

	/**
	 * View current list of JUL known loggers; if a JulLoggingManager is available, logger's modification for
	 * logger with a name starting by a JUL known logger name are propagated to JUL.
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

}