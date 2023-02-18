package it.openly.core.exceptions;

/**
 * @author filippo.possenti
 */
public class TooManyResultsException extends AppRuntimeException {
	public TooManyResultsException(String message, Object... arguments) {
		super(message, arguments);
	}
}
