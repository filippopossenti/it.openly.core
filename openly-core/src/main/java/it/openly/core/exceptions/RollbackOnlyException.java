package it.openly.core.exceptions;

/**
 * @author filippo.possenti
 */
public class RollbackOnlyException extends AppRuntimeException {
	public RollbackOnlyException(String message, Object... arguments) {
		super(message, arguments);
	}
}
