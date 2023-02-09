package igloo.julhelper.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JulLoggingManagerHolder {

	private static final Logger LOGGER = LoggerFactory.getLogger(JulLoggingManagerHolder.class);

	private static JulLoggingManager instance;

	public static synchronized JulLoggingManager register(JulLoggingManager bean) {
		JulLoggingManager previous = null;
		if (instance != null) {
			previous = instance;
			LOGGER.warn("A JulLoggingManager is already registered and is replaced");
		}
		instance = bean;
		return previous;
	}

	public static synchronized JulLoggingManager getInstance() {
		return instance;
	}

}
