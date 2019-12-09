package it.openly.core.exceptions;

/**
 * A specialised runtime exception that allow passing arguments.<br/>
 * The arguments are meant to be the arguments of the function that raised the exception or other information useful
 * to return meaningful error messages.
 * 
 * @author filippo.possenti
 */
public class AppRuntimeException extends RuntimeException {
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
