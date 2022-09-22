package igloo.julhelper.jmx;

import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class JulLoggingConfigurator {

	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(JulLoggingConfigurator.class);

	public Logger setLevel(final String name, final String level, Set<Logger> loggers, Map<String, Level> julLevelMapping) {
		Logger logger = getLogger(name, loggers);
		logger.setLevel(parseLevel(level, julLevelMapping));
		clearHandlers(logger);
		
		logger.setUseParentHandlers(false);
		logger.addHandler(new SLF4JBridgeHandler());
		loggers.add(logger);
		
		return logger;
	}

	public Level parseLevel(final String level, Map<String, Level> julLevelMapping) {
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
	public boolean matchJulKnownLoggers(String loggerName, Set<String> julKnownLoggers) {
		return julKnownLoggers.stream().anyMatch(loggerName::startsWith);
	}

	public void updateJulKnownLoggers(String updateSource, Supplier<Scanner> supplier, boolean reset, Set<String> julKnownLoggers) {
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
	public void doUnsetLevel(final String name, Set<Logger> loggers) {
		Logger logger = getLogger(name, loggers);
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
	private Logger getLogger(final String name, Set<Logger> loggers) {
		return loggers.stream().filter(i -> i.getName().equals(name))
				.findFirst()
				.orElseGet(() -> Logger.getLogger(name));
	}
}
