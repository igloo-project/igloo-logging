package igloo.julhelper.jmx;

import java.lang.management.ManagementFactory;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import igloo.julhelper.api.JulLoggingManager;
import igloo.julhelper.api.JulLoggingManagerHolder;

/**
 * Implementation for {@link JulLoggingManager} MBean. All public methods are synchronized to ensure
 * {@link #loggers} consistency.
 */
public class JulLoggingManagerMBean extends StandardMBean {

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
	 * @param resource path used to load well-known JUL logger names. Use null to disable loading.
	 * @see JulLoggingManager
	 */
	public static void registerMBean(String JulKnownLogger) {
		try {
			ObjectName objectName = new ObjectName(String.format("igloo:type=%s,name=%s", JulLoggingManagerMBean.TYPE, JulLoggingManagerMBean.NAME));
			MBeanServer server = ManagementFactory.getPlatformMBeanServer();
			JulLoggingManagerImpl manager = new JulLoggingManagerImpl(JulKnownLogger);
			server.registerMBean(new JulLoggingManagerMBean(manager), objectName);
			JulLoggingManagerHolder.register(manager);
		} catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException
				| NotCompliantMBeanException e) {
			throw new IllegalStateException(e);
		}
	}

	public JulLoggingManagerMBean(JulLoggingManager impl) {
		super(impl, JulLoggingManager.class, true);
	}

}
