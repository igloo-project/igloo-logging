package igloo.log4j2jmx.servlet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class Log4j2LoggingManagerListener extends AbstractLog4j2LoggingManagerListener
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
