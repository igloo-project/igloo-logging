package igloo.log4j2jmx.servlet;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

public class JakartaLog4j2LoggingManagerListener extends AbstractLog4j2LoggingManagerListener
		implements ServletContextListener {
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		super.contextInitialized();
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		super.contextDestroyed();
	}
}
