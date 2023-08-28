package igloo.julhelper.internal;

public class JakartaContextEventWrapper implements CommonContextEvent {

	private final jakarta.servlet.ServletContextEvent contextEvent;

	public JakartaContextEventWrapper(jakarta.servlet.ServletContextEvent contextEvent) {
		super();
		this.contextEvent = contextEvent;
	}

	@Override
	public String getInitParameter(String paramName) {
		return contextEvent.getServletContext().getInitParameter(paramName);
	}

}
