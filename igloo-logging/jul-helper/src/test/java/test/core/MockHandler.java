package test.core;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

/**
 * Only used to be "Spy"
 */
public class MockHandler extends Handler {

	@Override
	public void publish(LogRecord record) {
		// nothing
	}

	@Override
	public void flush() {
		// nothing
	}

	@Override
	public void close() throws SecurityException {
		// nothing
	}

}
