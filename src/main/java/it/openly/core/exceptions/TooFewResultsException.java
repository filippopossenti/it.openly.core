package it.openly.core.exceptions;

public class TooFewResultsException extends AppRuntimeException {
	private static final long serialVersionUID = 20127687493769473L;

	public TooFewResultsException(String message, Object... arguments) {
		super(message, arguments);
	}
	
	public TooFewResultsException(String message, Throwable cause, Object... arguments) {
		super(message, cause, arguments);
	}

}
