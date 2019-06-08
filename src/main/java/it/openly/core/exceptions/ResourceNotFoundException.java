package it.openly.core.exceptions;

/**
 * @author filippo.possenti
 */
public class ResourceNotFoundException extends AppRuntimeException {
	public ResourceNotFoundException(String message, Object... arguments) {
		super(message, arguments);
	}
}
