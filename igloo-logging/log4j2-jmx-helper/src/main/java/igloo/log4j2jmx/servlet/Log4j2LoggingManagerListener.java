package igloo.log4j2jmx.servlet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import igloo.log4j2jmx.jmx.Log4j2LoggingManager;
import igloo.log4j2jmx.jmx.Log4j2LoggingManagerMBean;

/**
 * Install a {@link Log4j2LoggingManager} MBean. This listener does not use any `context-param`.
 * 
 * @see Log4j2LoggingManager
 */
public class Log4j2LoggingManagerListener implements ServletContextListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(Log4j2LoggingManagerListener.class);

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		Log4j2LoggingManagerMBean.registerMBean();
		
		LOGGER.info("jul-to-slf4j installed");
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
	}

}
