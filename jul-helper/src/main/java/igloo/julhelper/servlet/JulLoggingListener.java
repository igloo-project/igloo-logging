package igloo.julhelper.servlet;

import javax.servlet.ServletContextListener;

import igloo.julhelper.internal.AbstractJulLoggingListener;
import igloo.julhelper.internal.JavaxContextEventWrapper;

/**
 * <code>javax.servlet</code> version.
 * 
 * @see AbstractJulLoggingListener
 */
public class JulLoggingListener extends AbstractJulLoggingListener implements ServletContextListener {

	@Override
	public void contextInitialized(javax.servlet.ServletContextEvent sce) {
		super.contextInitialized(new JavaxContextEventWrapper(sce));
	}

	@Override
	public void contextDestroyed(javax.servlet.ServletContextEvent sce) {
		super.contextDestroyed();
	}

}
