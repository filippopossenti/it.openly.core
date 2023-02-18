package it.openly.core.exceptions;

/**
 * @author filippo.possenti
 */
public class TooFewResultsException extends AppRuntimeException {
	public TooFewResultsException(String message, Object... arguments) {
		super(message, arguments);
	}
}
