package test.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.atIndex;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import igloo.log4j2jmx.jmx.Log4j2LoggingConfigurator;
import igloo.log4j2jmx.jmx.Log4j2LoggingConfigurator.LevelWrapper;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TestLog4j2LoggingConfigurator {

	private Log4j2LoggingConfigurator log4j2LoggingConfigurator;

	private final String loggerName = "loggerTest";

	@BeforeEach
	void setUp() {
		log4j2LoggingConfigurator = new Log4j2LoggingConfigurator();
	}

	@AfterEach
	void clean() {
		for (Entry<String, LoggerConfig> loggerConfig : ((LoggerContext) LogManager.getContext(false)).getConfiguration().getLoggers().entrySet()) {
			((LoggerContext) LogManager.getContext(false)).getConfiguration().removeLogger(loggerConfig.getKey());
		};
	}

	@Test
	void test_doSetLevel_newLocalLogger_newServerLogger() throws Exception {
		String level = "DEBUG";
		
		Set<Logger> localLoggers = new HashSet<>();
		Map<String, LevelWrapper> localOriginalLevels = new ConcurrentHashMap<>();
		
		log4j2LoggingConfigurator.doSetLevel(loggerName, level, localLoggers, localOriginalLevels);
		
		Map<String, LoggerConfig> serverLoggers = ((LoggerContext) LogManager.getContext(false)).getConfiguration().getLoggers();
		
		assertThat(new ArrayList<>(localLoggers))
			.hasSize(1)
			.satisfies(
				logger -> {
					assertThat(logger.getName()).isEqualTo(loggerName);
					assertThat(logger.getLevel().name()).isEqualTo(level);
				},
				atIndex(0)
			);
			
		assertThat(serverLoggers)
			.hasSize(1)
			.containsKey(loggerName)
			.satisfies(
				value -> {
					LoggerConfig loggerConfig = value.get(loggerName);
					assertThat(loggerConfig.getLevel().name()).isEqualTo(level);
				}
			);
		
		assertThat(localOriginalLevels)
			.hasSize(1)
			.containsKey(loggerName)
			.satisfies(
				value -> {
					LevelWrapper originalLevel = value.get(loggerName);
					assertThat(originalLevel.name()).isEqualTo("ERROR");
				}
			);
	}

	@Test
	void test_doSetLevel_newLocalLogger_setServerLogger() throws Exception {
		String level = "DEBUG";
		String levelOriginal = "TRACE";
		
		((LoggerContext) LogManager.getContext(false)).getConfiguration().addLogger(
			loggerName, new LoggerConfig(loggerName, Level.valueOf(levelOriginal), true)
		);
		
		Set<Logger> localLoggers = new HashSet<>();
		Map<String, LevelWrapper> localOriginalLevels = new ConcurrentHashMap<>();
		
		log4j2LoggingConfigurator.doSetLevel(loggerName, level, localLoggers, localOriginalLevels);
		
		Map<String, LoggerConfig> serverLoggers = ((LoggerContext) LogManager.getContext(false)).getConfiguration().getLoggers();
		
		assertThat(new ArrayList<>(localLoggers))
			.hasSize(1)
			.satisfies(
				logger -> {
					assertThat(logger.getName()).isEqualTo(loggerName);
					assertThat(logger.getLevel().name()).isEqualTo(level);
				},
				atIndex(0)
			);
			
		assertThat(serverLoggers)
			.hasSize(1)
			.containsKey(loggerName)
			.satisfies(
				value -> {
					LoggerConfig loggerConfig = value.get(loggerName);
					assertThat(loggerConfig.getLevel().name()).isEqualTo(level);
				}
			);
		
		assertThat(localOriginalLevels)
			.hasSize(1)
			.containsKey(loggerName)
			.satisfies(
				value -> {
					LevelWrapper originalLevel = value.get(loggerName);
					assertThat(originalLevel.name()).isEqualTo(levelOriginal);
				}
			);
	}

	@Test
	void test_doSetLevel_deleteLocalLogger_deleteServerLogger() throws Exception {
		String level = null;
		String levelOriginal = "TRACE";
		
		((LoggerContext) LogManager.getContext(false)).getConfiguration().addLogger(
			loggerName, new LoggerConfig(loggerName, Level.valueOf(levelOriginal), true)
		);
		
		Set<Logger> localLoggers = new HashSet<>();
		Map<String, LevelWrapper> localOriginalLevels = new ConcurrentHashMap<>();
		
		log4j2LoggingConfigurator.doSetLevel(loggerName, level, localLoggers, localOriginalLevels);
		
		Map<String, LoggerConfig> serverLoggers = ((LoggerContext) LogManager.getContext(false)).getConfiguration().getLoggers();
		
		assertThat(new ArrayList<>(localLoggers))
			.isEmpty();
			
		assertThat(serverLoggers)
			.isEmpty();
		
		assertThat(localOriginalLevels)
			.isEmpty();
	}

	@Test
	void test_doSetLevel_deleteLocalLogger_setServerLogger() throws Exception {
		String level = null;
		String levelOriginal = "TRACE";
		
		((LoggerContext) LogManager.getContext(false)).getConfiguration().addLogger(
			loggerName, new LoggerConfig(loggerName, Level.valueOf(levelOriginal), true)
		);
		
		Set<Logger> localLoggers = new HashSet<>();
		localLoggers.add((Logger) LogManager.getLogger(loggerName));
		Map<String, LevelWrapper> localOriginalLevels = new ConcurrentHashMap<>();
		localOriginalLevels.put(loggerName, new LevelWrapper(Level.valueOf(levelOriginal)));
		
		log4j2LoggingConfigurator.doSetLevel(loggerName, level, localLoggers, localOriginalLevels);
		
		Map<String, LoggerConfig> serverLoggers = ((LoggerContext) LogManager.getContext(false)).getConfiguration().getLoggers();
		
		assertThat(new ArrayList<>(localLoggers))
			.isEmpty();
			
		assertThat(serverLoggers)
			.hasSize(1)
			.containsKey(loggerName)
			.satisfies(
				value -> {
					LoggerConfig loggerConfig = value.get(loggerName);
					assertThat(loggerConfig.getLevel().name()).isEqualTo(levelOriginal);
				}
			);
		
		assertThat(localOriginalLevels)
			.isEmpty();
	}

	@Test
	void test_doUnsetLevel() throws Exception {
		log4j2LoggingConfigurator = Mockito.spy(log4j2LoggingConfigurator);
		
		Set<Logger> localLoggers = new HashSet<>();
		Map<String, LevelWrapper> localOriginalLevels = new ConcurrentHashMap<>();
		
		doReturn(null).when(log4j2LoggingConfigurator).doSetLevel(any(), any(), any(), any());
		
		log4j2LoggingConfigurator.doUnsetLevel(loggerName, localLoggers, localOriginalLevels);
		
		verify(log4j2LoggingConfigurator, times(1)).doSetLevel(loggerName, null, localLoggers, localOriginalLevels);
	}

	@Test
	void test_popOriginalLevel() throws Exception {
		String levelOriginal = "TRACE";
		
		Map<String, LevelWrapper> localOriginalLevels = new ConcurrentHashMap<>();
		localOriginalLevels.put(loggerName, new LevelWrapper(Level.valueOf(levelOriginal)));
		
		Level result = log4j2LoggingConfigurator.popOriginalLevel(loggerName, localOriginalLevels);
		
		assertThat(result.name()).isEqualTo(levelOriginal);
		assertThat(localOriginalLevels).isEmpty();
	}

	@Test
	void test_popOriginalLevel_returnNull() throws Exception {
		String levelOriginal = "TRACE";
		
		Map<String, LevelWrapper> localOriginalLevels = new ConcurrentHashMap<>();
		localOriginalLevels.put(loggerName, new LevelWrapper(Level.valueOf(levelOriginal)));
		
		Level result = log4j2LoggingConfigurator.popOriginalLevel("loggerUnknown", localOriginalLevels);
		
		assertThat(result).isNull();
		assertThat(localOriginalLevels).hasSize(1);
	}

	@Test
	void test_getLogger() throws Exception {
		Set<Logger> localLoggers = new HashSet<>();
		localLoggers.add((Logger) LogManager.getLogger(loggerName));
		localLoggers.add((Logger) LogManager.getLogger("otherLoggerTest"));
		
		Logger result = log4j2LoggingConfigurator.getLogger(loggerName, localLoggers);
		
		assertThat(localLoggers).contains(result);
		assertThat(result.getName()).isEqualTo(loggerName);
	}

	@Test
	void test_getLogger_newLogger() throws Exception {
		String loggerExpected = "loggerExpected";
		
		Set<Logger> localLoggers = new HashSet<>();
		localLoggers.add((Logger) LogManager.getLogger(loggerName));
		localLoggers.add((Logger) LogManager.getLogger("otherLoggerTest"));
		
		Logger result = log4j2LoggingConfigurator.getLogger(loggerExpected, localLoggers);
		
		assertThat(localLoggers).doesNotContain(result);
		assertThat(result.getName()).isEqualTo(loggerExpected);
	}
}
