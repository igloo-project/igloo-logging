package igloo.log4j2jmx.servlet;

import javax.management.ObjectName;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import igloo.julhelper.internal.AbstractJulLoggingListener;
import igloo.log4j2jmx.jmx.Log4j2LoggingManager;
import igloo.log4j2jmx.jmx.Log4j2LoggingManagerMBean;

/**
 * Install a {@link Log4j2LoggingManager} MBean. This listener does not use any `context-param`. This logging manager
 * installs a MBean that allows to dynamically reconfigure log4j2 loggers (log4j2 default implementation only
 * allows to reconfigure already configured loggers).
 * 
 * {@link AbstractJulLoggingListener} (jul-helper dependency) is a needed companion if you want to reconfigure JUL loggers at runtime.
 * julKnownLoggers configuration (runtime or startup) may need customization to allow JUL logging override. See
 * {@link AbstractJulLoggingListener} for further documentation.
 * 
 * `igloo-logging:jul-helper` dependency must be added to use JUL reconfiguration.
 * 
 * Commplete default setup :
 * 
 * <pre>{@code
 * <listener>
 *   <listener-class>igloo.julhelper.servlet.JulLoggingListener</listener-class>
 * </listener>
 * <listener>
 *   <listener-class>igloo.log4j2jmx.servlet.Log4j2LoggingManagerListener</listener-class>
 * </listener>
 * }</pre>
 * 
 * @see Log4j2LoggingManager
 */
public abstract class AbstractLog4j2LoggingManagerListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractLog4j2LoggingManagerListener.class);

	private ObjectName mbeanObjectName;

	public void contextInitialized() {
		mbeanObjectName = Log4j2LoggingManagerMBean.registerMBean();
		
		LOGGER.info("jul-to-slf4j installed");
	}

	public void contextDestroyed() {
		if (mbeanObjectName != null) {
			Log4j2LoggingManagerMBean.unregisterMBean(mbeanObjectName);
		}
	}

}
