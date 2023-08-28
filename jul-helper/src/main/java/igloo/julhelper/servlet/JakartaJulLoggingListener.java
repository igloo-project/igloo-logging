package igloo.julhelper.servlet;

import igloo.julhelper.internal.AbstractJulLoggingListener;
import igloo.julhelper.internal.JakartaContextEventWrapper;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

/**
 * <code>jakarta.servlet</code> version.
 * 
 * @see AbstractJulLoggingListener
 */
public class JakartaJulLoggingListener extends AbstractJulLoggingListener implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		super.contextInitialized(new JakartaContextEventWrapper(sce));
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		super.contextDestroyed();
	}

}
