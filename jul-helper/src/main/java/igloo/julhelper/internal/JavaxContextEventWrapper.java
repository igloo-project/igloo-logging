package igloo.julhelper.internal;

import javax.servlet.ServletContextEvent;

public class JavaxContextEventWrapper implements CommonContextEvent {

	private final ServletContextEvent contextEvent;

	public JavaxContextEventWrapper(ServletContextEvent contextEvent) {
		super();
		this.contextEvent = contextEvent;
	}

	@Override
	public String getInitParameter(String paramName) {
		return contextEvent.getServletContext().getInitParameter(paramName);
	}

}
