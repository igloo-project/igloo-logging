package igloo.log4j2jmx.jmx;

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

public class Log4j2LoggingManagerMBean extends StandardMBean {

	public static final Object DOMAIN = "igloo";

	public static final Object TYPE = "LoggingManager";

	public static final Object NAME = Log4j2LoggingManager.class.getSimpleName();

	/**
	 * Register `igloo:type=LoggingManager,name=Log4j2LoggingManagerMBean`.
	 * 
	 * @seee {@link Log4j2LoggingManager}
	 */
	public static void registerMBean() {
		final MBeanServer server = ManagementFactory.getPlatformMBeanServer();
		JulLoggingManager julHelper = JulLoggingManagerHolder.getInstance();
		try {
			ObjectName objectName = new ObjectName(String.format("%s:type=%s,name=%s", DOMAIN, TYPE, NAME));
			server.registerMBean(new Log4j2LoggingManagerMBean(julHelper), objectName);
		} catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException
				| NotCompliantMBeanException e) {
			throw new IllegalStateException(e);
		}
	}

	public Log4j2LoggingManagerMBean() {
		this(null);
	}

	public Log4j2LoggingManagerMBean(JulLoggingManager julHelper) {
		super(new Log4j2LoggingManagerImpl(julHelper), Log4j2LoggingManager.class, true);
	}
}
