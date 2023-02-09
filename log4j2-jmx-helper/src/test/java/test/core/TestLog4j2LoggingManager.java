package test.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import igloo.julhelper.api.JulLoggingManager;
import igloo.log4j2jmx.jmx.Log4j2LoggingConfigurator;
import igloo.log4j2jmx.jmx.Log4j2LoggingManagerImpl;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TestLog4j2LoggingManager {

	@Mock
	private JulLoggingManager julLoggingManagerMock;

	@Mock
	private Log4j2LoggingConfigurator log4j2LoggingConfiguratorMock;

	private Log4j2LoggingManagerImpl log4j2LoggingManager;

	private final String loggerName = "loggerTest";

	private LoggerConfig logginConfig;

	@BeforeEach
	void setUp() {
		log4j2LoggingManager = new Log4j2LoggingManagerImpl(julLoggingManagerMock, log4j2LoggingConfiguratorMock);
		log4j2LoggingManager = spy(log4j2LoggingManager);
		
		logginConfig = new LoggerConfig(loggerName, Level.valueOf("DEBUG"), true);
		((LoggerContext) LogManager.getContext(false)).getConfiguration().addLogger(
			loggerName, logginConfig
		);
	}

	@AfterEach
	void clean() {
		for (Entry<String, LoggerConfig> loggerConfig : ((LoggerContext) LogManager.getContext(false)).getConfiguration().getLoggers().entrySet()) {
			((LoggerContext) LogManager.getContext(false)).getConfiguration().removeLogger(loggerConfig.getKey());
		};
	}

	@Test
	void test_setLevel() throws Exception {
		String levelExpected = "TRACE";
		doReturn(LogManager.getLogger(loggerName)).when(log4j2LoggingConfiguratorMock).doSetLevel(any(), any(), any(), any());
		
		log4j2LoggingManager.setLevel(loggerName, levelExpected);
		
		verify(log4j2LoggingConfiguratorMock, times(1)).doSetLevel(eq(loggerName), eq(levelExpected), any(), any());
		verify(julLoggingManagerMock, times(1)).setLevelIfWellKnown(loggerName, levelExpected);
	}

	@Test
	void test_unsetLevel() throws Exception {
		log4j2LoggingManager.unsetLevel(loggerName);
		
		verify(log4j2LoggingConfiguratorMock, times(1)).doUnsetLevel(eq(loggerName), any(), any());
		verify(julLoggingManagerMock, times(1)).unsetLevel(loggerName);
	}

	@Test
	void test_reset() throws Exception {
		Logger loggerExpected = (Logger) LogManager.getLogger("loggerExpected");
		Logger loggerOtherExpected = (Logger) LogManager.getLogger("loggerOtherExpected");
		log4j2LoggingManager.getLoggers().add(loggerExpected);
		log4j2LoggingManager.getLoggers().add(loggerOtherExpected);
		
		log4j2LoggingManager.reset();
		
		verify(log4j2LoggingConfiguratorMock, times(2)).doUnsetLevel(any(), any(), any());
		verify(log4j2LoggingConfiguratorMock, times(1)).doUnsetLevel(eq(loggerExpected.getName()), any(), any());
		verify(log4j2LoggingConfiguratorMock, times(1)).doUnsetLevel(eq(loggerOtherExpected.getName()), any(), any());
		verify(julLoggingManagerMock, times(2)).unsetLevel(any());
		verify(julLoggingManagerMock, times(1)).unsetLevel(loggerExpected.getName());
		verify(julLoggingManagerMock, times(1)).unsetLevel(loggerOtherExpected.getName());
	}

	@Test
	void test_getJulKnownLoggers() throws Exception {
		Set<String> loggersExpected = Set.of();
		
		when(julLoggingManagerMock.getJulKnownLoggers()).thenReturn(loggersExpected);
		
		Set<String> result = log4j2LoggingManager.getJulKnownLoggers();
		
		assertThat(result).isEqualTo(loggersExpected);
		verify(julLoggingManagerMock, times(1)).getJulKnownLoggers();
	}

	@Test
	void test_getJulKnownLoggers_isNull() throws Exception {
		Set<String> result = log4j2LoggingManager.getJulKnownLoggers();
		
		assertThat(result).isEmpty();
		verify(julLoggingManagerMock, times(1)).getJulKnownLoggers();
	}

	@Test
	void test_addJulKnownLoggers() throws Exception {
		log4j2LoggingManager.addJulKnownLoggers(loggerName);
		
		verify(julLoggingManagerMock, times(1)).addJulKnownLoggers(loggerName);
	}

	@Test
	void test_updateJulKnownLoggers() throws Exception {
		log4j2LoggingManager.updateJulKnownLoggers(loggerName);
		
		verify(julLoggingManagerMock, times(1)).updateJulKnownLoggers(loggerName);
	}

	@Test
	void test_getJulLoggingManagementEnabled() throws Exception {
		boolean result = log4j2LoggingManager.getJulLoggingManagementEnabled();
		
		assertThat(result).isTrue();
	}

	@Test
	void test_getJulLoggingManagementEnabled_isDisabled() throws Exception {
		log4j2LoggingManager = new Log4j2LoggingManagerImpl();
		
		boolean result = log4j2LoggingManager.getJulLoggingManagementEnabled();
		
		assertThat(result).isFalse();
	}

	@Test
	void test_getLoggerNames() throws Exception {
		Logger loggerExpected = (Logger) LogManager.getLogger("loggerExpected");
		Logger loggerOtherExpected = (Logger) LogManager.getLogger("loggerOtherExpected");
		
		log4j2LoggingManager.getLoggers().add(loggerExpected);
		log4j2LoggingManager.getLoggers().add(loggerOtherExpected);
		
		List<String> results = log4j2LoggingManager.getLoggerNames();
		
		assertThat(results)
			.hasSize(2)
			.containsExactly(loggerExpected.getName(), loggerOtherExpected.getName());
	}

	@Test
	void test_getLoggerConfig() throws Exception {
		Logger loggerExpected = (Logger) LogManager.getLogger("loggerExpected");
		Logger loggerOtherExpected = (Logger) LogManager.getLogger("loggerOtherExpected");
		
		log4j2LoggingManager.getLoggers().add(loggerExpected);
		log4j2LoggingManager.getLoggers().add(loggerOtherExpected);
		
		String result = log4j2LoggingManager.getLoggerConfig();
		
		assertThat(result).isEqualTo(
			"loggerExpected=ERROR (original: NONE)\n"
			+ "loggerOtherExpected=ERROR (original: NONE)"
		);

	}

}
