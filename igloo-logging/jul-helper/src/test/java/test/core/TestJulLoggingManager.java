package test.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import igloo.julhelper.jmx.JulLoggingConfigurator;
import igloo.julhelper.jmx.JulLoggingManagerImpl;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TestJulLoggingManager {

	@Mock
	private JulLoggingConfigurator julLoggingConfiguratorMock;

	private JulLoggingManagerImpl julLoggingManager;

	private final String loggerName = "loggerTest";

	@BeforeEach
	void setUp() {
		julLoggingManager = new JulLoggingManagerImpl("", julLoggingConfiguratorMock);
	}

	@Test
	void test_getLoggerNames() throws Exception {
		Logger loggerExpected = Logger.getLogger("loggerExpected");
		Logger loggerOtherExpected = Logger.getLogger("loggerOtherExpected");
		
		julLoggingManager.getLoggers().add(loggerExpected);
		julLoggingManager.getLoggers().add(loggerOtherExpected);
		
		List<String> results = julLoggingManager.getLoggerNames();
		
		assertThat(results)
			.hasSize(2)
			.containsExactly(loggerExpected.getName(), loggerOtherExpected.getName());
	}

	@Test
	void test_getLoggerConfig() throws Exception {
		Logger loggerExpected = Logger.getLogger("loggerExpected");
		loggerExpected.setLevel(Level.INFO);
		Logger loggerOtherExpected = Logger.getLogger("loggerOtherExpected");
		loggerOtherExpected.setLevel(Level.INFO);
		
		julLoggingManager.getLoggers().add(loggerExpected);
		julLoggingManager.getLoggers().add(loggerOtherExpected);
		
		String result = julLoggingManager.getLoggerConfig();
		
		assertThat(result).isEqualTo(
			"loggerExpected=INFO\n"
			+ "loggerOtherExpected=INFO"
		);
	}

	@Test
	void test_setLevel() throws Exception {
		String level = "INFO";
		
		julLoggingManager.setLevel(loggerName, level);
		
		verify(julLoggingConfiguratorMock, times(1)).setLevel(eq(loggerName), eq(level), any(), any());
	}

	@Test
	void test_setLevelIfWellKnown_setLevel() throws Exception {
		julLoggingManager = Mockito.spy(julLoggingManager);
		String level = "INFO";
		
		when(julLoggingConfiguratorMock.matchJulKnownLoggers(eq(loggerName), any())).thenReturn(true);
		doNothing().when(julLoggingManager).setLevel(loggerName, level);
		
		julLoggingManager.setLevelIfWellKnown(loggerName, level);
		
		verify(julLoggingManager, times(1)).setLevel(loggerName, level);
	}

	@Test
	void test_setLevelIfWellKnown_notSetLevel() throws Exception {
		julLoggingManager = Mockito.spy(julLoggingManager);
		String level = "INFO";
		
		when(julLoggingConfiguratorMock.matchJulKnownLoggers(eq(loggerName), any())).thenReturn(false);
		doNothing().when(julLoggingManager).setLevel(loggerName, level);
		
		julLoggingManager.setLevelIfWellKnown(loggerName, level);
		
		verify(julLoggingManager, never()).setLevel(loggerName, level);
	}

	@Test
	void test_unsetLevel() throws Exception {
		doNothing().when(julLoggingConfiguratorMock).doUnsetLevel(loggerName, julLoggingManager.getLoggers());
		
		julLoggingManager.unsetLevel(loggerName);
		
		verify(julLoggingConfiguratorMock, times(1)).doUnsetLevel(loggerName, julLoggingManager.getLoggers());
	}

	@Test
	void test_reset() throws Exception {
		Logger loggerExpected = Logger.getLogger("loggerExpected");
		Logger loggerOtherExpected = Logger.getLogger("loggerOtherExpected");
		
		julLoggingManager.getLoggers().add(loggerExpected);
		julLoggingManager.getLoggers().add(loggerOtherExpected);
		
		julLoggingManager.reset();
		
		verify(julLoggingConfiguratorMock, times(2)).doUnsetLevel(any(), any());
		verify(julLoggingConfiguratorMock, times(1)).doUnsetLevel(loggerExpected.getName(), julLoggingManager.getLoggers());
		verify(julLoggingConfiguratorMock, times(1)).doUnsetLevel(loggerOtherExpected.getName(), julLoggingManager.getLoggers());
	}

	@Test
	void test_addJulKnownLoggers() throws Exception {
		julLoggingManager.addJulKnownLoggers("julLoggerName");
		
		// called in constructor and method
		verify(julLoggingConfiguratorMock, times(2)).updateJulKnownLoggers(any(), any(), eq(false), any());
	}

	@Test
	void test_updateJulKnownLoggers() throws Exception {
		julLoggingManager.updateJulKnownLoggers("julLoggerName");
		
		verify(julLoggingConfiguratorMock, times(1)).updateJulKnownLoggers(any(), any(), eq(true), any());
	}

}
