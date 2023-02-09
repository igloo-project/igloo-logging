package igloo.log4j2jmx.jmx;

import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;

public class Log4j2LoggingConfigurator {

	public Logger doSetLevel(final String name, final String levelAsString, Set<Logger> loggers, Map<String, LevelWrapper> originalLevels) {
		// getting or creating a logger
		Logger logger = getLogger(name, loggers);
		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		Configuration conf = ctx.getConfiguration();
		// server logger
		LoggerConfig lconf = conf.getLoggerConfig(name);
		
		// 
		if (levelAsString != null) {
			// server logger level
			Level originalLevel = lconf.getLevel();
			// add server logger
			if (!lconf.getName().equals(logger.getName())) {
				lconf = new LoggerConfig(logger.getName(), Level.valueOf(levelAsString), true);
				conf.addLogger(logger.getName(), lconf);
			// set existing server logger
			} else {
				lconf.setLevel(Level.valueOf(levelAsString));
			}
			// add the original level if not yet in local
			originalLevels.computeIfAbsent(name, (n) -> new LevelWrapper(originalLevel));
		// reset the logger level to default
		} else {
			Level targetLevel = popOriginalLevel(name, originalLevels);
			// no default level -> delete server logger
			if (targetLevel == null) {
				conf.removeLogger(name);
			// set level server logger
			} else {
				lconf.setLevel(targetLevel);
			}
		}
		
		ctx.updateLoggers(conf);
		
		// no level means that the local logger is deleted
		if (levelAsString == null) {
			loggers.remove(logger);
		// add logger only if it does not exist yet
		} else {
			loggers.add(logger);
		}
		return logger;
	}

	public Logger getLogger(final String name, Set<Logger> loggers) {
		return loggers.stream().filter(i -> i.getName().equals(name))
			.findFirst()
			.orElseGet(() -> (Logger) LogManager.getLogger(name));
	}

	public Level popOriginalLevel(final String name, Map<String, LevelWrapper> originalLevels) {
		if (originalLevels.containsKey(name)) {
			return originalLevels.remove(name).level;
		} else {
			return null;
		}
	}

	public void doUnsetLevel(final String name, Set<Logger> loggers, Map<String, LevelWrapper> originalLevels) {
		doSetLevel(name, null, loggers, originalLevels);
	}

	/**
	 * This class allows to store null {@link Level} values in {@link Log4j2LoggingManagerImpl#loggers}.
	 */
	public static class LevelWrapper {
		private final Level level;
		
		public LevelWrapper(Level level) {
			this.level = level;
		}
		
		public String name() {
			return this.level != null ? this.level.name() : "NONE";
		}
	}

}
