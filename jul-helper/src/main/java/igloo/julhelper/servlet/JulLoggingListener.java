package igloo.julhelper.servlet;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.LogManager;

import javax.management.ObjectName;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import igloo.julhelper.jmx.JulLoggingManagerMBean;

/**
 * # Usage
 * 
 * Conditionally install {@link SLF4JBridgeHandler} and `igloo:type=LoggingManager,name=JulLoggingManager` JMX Bean.
 * 
 * Behavior can be controlled with `skipJulSlf4jBridgeHandler` and `skipJulJmxHelper` init parameters.
 * Default behavior is to add both helpers.
 * 
 * `julKnownLoggersResourcePath` init parameter allows to point a resource for loading well-known JUL logger names.
 * This file must contain a line by JUL logger. This list is used to conditionally apply logger updates from
 * third-party components (like Log4j2LoggingManager), so that JUL logger configuration is applied only for loggers
 * and children from this list. By default, `jul-helper/well-known-jul-loggers.txt` is used. You can disable
 * this loading with the value `none`. Effective configuration can be retrieve from JMX bean.
 * 
 * This listener can be added in `web.xml` by adding this extract among the first listeners (`context-param` may be
 * omitted if default values are convenient):
 * 
 * ```xml
 * <listener>
 *   <listener-class>igloo.julhelper.servlet.JulLoggingListener</listener-class>
 * </listener>
 * <context-param>
 *   <param-name>skipJulSlf4jBridgeHandler</param-name>
 *   <param-value>false</param-value>
 * </context-param>
 * <context-param>
 *   <param-name>skipJulJmxHelper</param-name>
 *   <param-value>false</param-value>
 * </context-param>
 * ```
 * 
 * # About SLF4JBridgeHandler
 * 
 * Purpose of SLF4JBridgeHandler is to install a handler on the `java.util.logging` root logger. All event that can be
 * forwarded from child logger and accepted by root logger level can then be handled by SLF4J.
 * 
 * It implies that level must be set accordingly so that log event can be propagated to SLF4J bridge handler. Either:
 * 
 * * Set a catch-all root level, and do not set level on any child. This configuration implies a performance hit as
 *   all log event are propagated;
 * * Set a catch-all root level, set warning or error level to limit propagation of unwanted loggers. This allows to
 *   limit log event volume. This configuration needs to configure both JUL and SLF4J backend consistently;
 * * Set a WARN root level, and attach {@link SLF4JBridgeHandler} on each targeted child logger. JUL and SLF4J
 *   backend need to be configured consistently.
 *   
 * # About JUL JMX helper
 * 
 * This JMX MBean allows to bind {@link SLF4JBridgeHandler} and set level on arbitrary loggers to activate logging
 * during runtime.
 */
public class JulLoggingListener implements ServletContextListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(JulLoggingListener.class);

	private static final String PARAMETER_SKIP_JUL_SLF4J_BRIDGE_HANDLER = "skipJulSlf4jBridgeHandler";
	private static final String PARAMETER_SKIP_JUL_JMX_HELPER = "skipJulJmxHelper";
	private static final String PARAMETER_JUL_KNOWN_LOGGERS_RESOURCE_PATH = "julKnownLoggersResourcePath";

	private ObjectName mbeanObjectName;

	/**
	 * @see JulLoggingListener
	 */
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		// "" triggers default loading
		String julKnownLoggersResourcePath = Optional.ofNullable(sce.getServletContext().getInitParameter(PARAMETER_JUL_KNOWN_LOGGERS_RESOURCE_PATH)).orElse("");
		if ("none".equalsIgnoreCase(julKnownLoggersResourcePath)) {
			// disable loading
			julKnownLoggersResourcePath = null;
		}
		
		if (!getBooleanParameter(sce, PARAMETER_SKIP_JUL_SLF4J_BRIDGE_HANDLER)) {
			initSlf4jBridgeHandler();
		}
		
		if (!getBooleanParameter(sce, PARAMETER_SKIP_JUL_JMX_HELPER)) {
			mbeanObjectName = JulLoggingManagerMBean.registerMBean(julKnownLoggersResourcePath);
		}
		
		LOGGER.info("jul-to-slf4j installed");
	}

	/**
	 * Install jul-to-slf4j bridge handler.
	 */
	private void initSlf4jBridgeHandler() {
		LogManager.getLogManager().reset();
		java.util.logging.Logger.getLogger("").setLevel(Level.WARNING);
		SLF4JBridgeHandler.removeHandlersForRootLogger();
		SLF4JBridgeHandler.install();
	}

	/**
	 * @see JulLoggingListener
	 */
	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		if (mbeanObjectName != null) {
			JulLoggingManagerMBean.unregisterMBean(mbeanObjectName);
		}
	}

	/**
	 * Extract boolean parameter named `paramName` from `sce`. Returns true only if parameter value is `true`.
	 * 
	 * @param sce configuration provider. Required.
	 * @param paramName name of the boolean parameter to extract. Required.
	 * @return true if parameter string value is `true`. false for other values or missing parameter.
	 */
	private boolean getBooleanParameter(ServletContextEvent sce, String paramName) {
		String param = sce.getServletContext().getInitParameter(paramName);
		return Boolean.TRUE.toString().equals(param);
	}

}
