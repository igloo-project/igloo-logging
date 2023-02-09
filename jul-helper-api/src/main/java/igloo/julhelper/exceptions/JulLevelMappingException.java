package igloo.julhelper.exceptions;

public class JulLevelMappingException extends RuntimeException {

	private static final long serialVersionUID = -47190215991942066L;

	public JulLevelMappingException() {
		super();
	}

	public JulLevelMappingException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public JulLevelMappingException(String message, Throwable cause) {
		super(message, cause);
	}

	public JulLevelMappingException(String message) {
		super(message);
	}

	public JulLevelMappingException(Throwable cause) {
		super(cause);
	}

}
