package test.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import igloo.julhelper.jmx.JulLoggingConfigurator;

class TestJulLoggingConfigurator {

	private JulLoggingConfigurator julLoggingConfigurator;

	@BeforeEach
	void setUp() {
		julLoggingConfigurator = new JulLoggingConfigurator();
	}

	@Test
	void test_setLevel() {
		String loggerName = "loggerTest";
		String level = "INFO";
		
		Map<String, Level> julLevelMapping = new HashMap<>();
		Set<Logger> loggers = ConcurrentHashMap.newKeySet();
		loggers.add(Logger.getLogger("otherLogger"));
		
		Logger result = julLoggingConfigurator.setLevel(loggerName, level, loggers, julLevelMapping);
		
		assertThat(result.getName()).isEqualTo(loggerName);
		assertThat(result.getLevel().getName()).isEqualTo(level);
		assertThat(loggers)
			.hasSize(2)
			.contains(result);
	}

	@Test
	void test_setLevel_log() {
		String loggerName = "loggerTest";
		MockHandler mockHandler = Mockito.spy(new MockHandler());
		
		Logger logger = Logger.getLogger(loggerName);
		
		Map<String, Level> julLevelMapping = new HashMap<>();
		Set<Logger> loggers = ConcurrentHashMap.newKeySet();
		loggers.add(logger);
		logger.addHandler(mockHandler);
		
		logger.warning("log");
		logger.fine("log hidded");
		julLoggingConfigurator.setLevel(loggerName, Level.FINE.getName(), loggers, julLevelMapping);
		logger.addHandler(mockHandler);
		Logger.getLogger(loggerName).fine("log");
		
		verify(mockHandler, times(2)).publish(any());
	}

	@Test
	void test_doUnsetLevel() {
		String loggerName = "loggerTest";
		
		Logger logger = Logger.getLogger(loggerName);
		
		Set<Logger> loggers = ConcurrentHashMap.newKeySet();
		loggers.add(Logger.getLogger("otherLogger"));
		loggers.add(logger);
		
		julLoggingConfigurator.doUnsetLevel(loggerName, loggers);
		
		assertThat(loggers)
			.hasSize(1)
			.doesNotContain(logger);
	}

	@Test
	void test_doUnsetLevel_log() {
		String loggerName = "loggerTest";
		MockHandler mockHandler = Mockito.spy(new MockHandler());
		
		Logger logger = Logger.getLogger(loggerName);
		logger.setLevel(Level.FINE);
		logger.addHandler(mockHandler);
		Set<Logger> loggers = ConcurrentHashMap.newKeySet();
		loggers.add(logger);
		
		logger.warning("log");
		logger.fine("log");
		julLoggingConfigurator.doUnsetLevel(loggerName, loggers);
		logger.addHandler(mockHandler);
		logger.fine("log hidded");
		
		verify(mockHandler, times(2)).publish(any());
	}

	@Test
	void test_parseLevel() {
		String level = "INFO";
		Map<String, Level> julLevelMapping = new HashMap<>();
		
		Level result = julLoggingConfigurator.parseLevel(level, julLevelMapping);
		
		assertThat(result).isEqualTo(Level.INFO);
	}

	@Test
	void test_parseLevel_levelMapping() {
		String level = "OTHER_LEVEL";
		Map<String, Level> julLevelMapping = new HashMap<>();
		julLevelMapping.put(level, Level.FINE);
		julLevelMapping.put("testLevel", Level.CONFIG);
		
		Level result = julLoggingConfigurator.parseLevel(level, julLevelMapping);
		
		assertThat(result).isEqualTo(Level.FINE);
	}

	@Test
	void test_parseLevel_levelUnknown() {
		String level = "OTHER_LEVEL";
		Map<String, Level> julLevelMapping = new HashMap<>();
		
		assertThatThrownBy(() -> {
			julLoggingConfigurator.parseLevel(level, julLevelMapping);
		}).isInstanceOf(RuntimeException.class);
	}

	@Test
	void test_matchJulKnownLoggers() {
		String loggerName = "loggerTest";
		
		Set<String> julKnownLoggers = new TreeSet<>();
		julKnownLoggers.add(loggerName);
		julKnownLoggers.add("otherLogger");
		
		boolean result = julLoggingConfigurator.matchJulKnownLoggers(loggerName, julKnownLoggers);
		
		assertThat(result).isTrue();
	}

	@Test
	void test_matchJulKnownLoggers_notMatch() {
		String loggerName = "loggerTest";
		
		Set<String> julKnownLoggers = new TreeSet<>();
		julKnownLoggers.add("otherLogger");
		
		boolean result = julLoggingConfigurator.matchJulKnownLoggers(loggerName, julKnownLoggers);
		
		assertThat(result).isFalse();
	}

	@Test
	void test_updateJulKnownLoggers() {
		String loggerExpected = "loggerTest";
		String otherLoggerExpected = "otherLoggerTest";
		String loggerNotExpected = "#loggerNotExpected";
		String loggerExists = "loggerExists";
		
		Set<String> julKnownLoggers = new TreeSet<>();
		julKnownLoggers.add(loggerExists);
		Supplier<Scanner> supplier = () -> new Scanner(loggerExpected + "\n" + otherLoggerExpected + "\n" + loggerNotExpected);
		
		julLoggingConfigurator.updateJulKnownLoggers("source", supplier, false, julKnownLoggers);
		
		assertThat(julKnownLoggers)
			.hasSize(3)
			.containsExactly(loggerExists, loggerExpected, otherLoggerExpected);
	}

	@Test
	void test_updateJulKnownLoggers_withReset() {
		String loggerExpected = "loggerTest";
		String otherLoggerExpected = "otherLoggerTest";
		String loggerNotExpected = "#loggerNotExpected";
		String loggerExists = "loggerExists";
		
		Set<String> julKnownLoggers = new TreeSet<>();
		julKnownLoggers.add(loggerExists);
		Supplier<Scanner> supplier = () -> new Scanner(loggerExpected + "\n" + otherLoggerExpected + "\n" + loggerNotExpected);
		
		julLoggingConfigurator.updateJulKnownLoggers("source", supplier, true, julKnownLoggers);
		
		assertThat(julKnownLoggers)
			.hasSize(2)
			.containsExactly(loggerExpected, otherLoggerExpected);
	}

}
