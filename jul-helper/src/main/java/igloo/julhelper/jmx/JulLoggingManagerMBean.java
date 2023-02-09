package igloo.julhelper.jmx;

import java.lang.management.ManagementFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import igloo.julhelper.api.JulLoggingManager;
import igloo.julhelper.api.JulLoggingManagerHolder;

/**
 * MBean registration handling.
 */
public class JulLoggingManagerMBean extends StandardMBean {

	private static final Logger LOGGER = LoggerFactory.getLogger(JulLoggingManagerMBean.class);

	public static final Object TYPE = "LoggingManager";
	public static final Object NAME = JulLoggingManager.class.getSimpleName();

	/**
	 * @see #registerMBean(String)
	 */
	public static void registerMBean() {
		registerMBean(null);
	}

	/**
	 * Install `igloo:type=LoggingManager,name=JulLoggingManager` JMX MBean.
	 * 
	 * @param julKnownLogger path used to load well-known JUL logger names. Use null to disable loading.
	 * @return added MBean {@link ObjectName}
	 * 
	 * @see JulLoggingManager
	 */
	public static ObjectName registerMBean(String julKnownLogger) {
		try {
			ObjectName objectName = new ObjectName(String.format("igloo:type=%s,name=%s", JulLoggingManagerMBean.TYPE, JulLoggingManagerMBean.NAME));
			MBeanServer server = ManagementFactory.getPlatformMBeanServer();
			JulLoggingManagerImpl manager = new JulLoggingManagerImpl(julKnownLogger);
			server.registerMBean(new JulLoggingManagerMBean(manager), objectName);
			JulLoggingManagerHolder.register(manager);
			return objectName;
		} catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException
				| NotCompliantMBeanException e) {
			throw new IllegalStateException(e);
		}
	}

	public static void unregisterMBean(ObjectName objectName) {
		final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
		try {
			server.unregisterMBean(objectName);
		} catch (MBeanRegistrationException|InstanceNotFoundException e) {
			LOGGER.warn("Unregistering {} failed.", objectName, e);
		}
	}

	public JulLoggingManagerMBean(JulLoggingManager impl) {
		super(impl, JulLoggingManager.class, true);
	}

}
