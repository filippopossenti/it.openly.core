package it.openly.core.exceptions;

public class TooManyResultsException extends AppRuntimeException {
	private static final long serialVersionUID = 20127687493769473L;

	public TooManyResultsException(String message, Object... arguments) {
		super(message, arguments);
	}
	
	public TooManyResultsException(String message, Throwable cause, Object... arguments) {
		super(message, cause, arguments);
	}

}
