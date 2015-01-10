package it.openly.core.exceptions;

/**
 * A specialised runtime exception that allow passing arguments, which will be
 * eventually used to format the message.
 * 
 * @author Filippo
 * 
 */
public class AppRuntimeException extends RuntimeException {
	private static final long serialVersionUID = 3861642153244385129L;

	private Object[] arguments;

	public AppRuntimeException(String message, Object... arguments) {
		super(String.format(message, arguments));
		this.arguments = arguments;
	}

	public AppRuntimeException(String message, Throwable cause, Object... arguments) {
		super(String.format(message, arguments), cause);
		this.arguments = arguments;
	}

	public Object[] getArguments() {
		return arguments;
	}
}
