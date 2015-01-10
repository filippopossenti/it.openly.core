package it.openly.core.exceptions;

public class ResourceNotFoundException extends AppRuntimeException {
	private static final long serialVersionUID = 20127687493769473L;

	public ResourceNotFoundException(String message, Object... arguments) {
		super(message, arguments);
	}
	
	public ResourceNotFoundException(String message, Throwable cause, Object... arguments) {
		super(message, cause, arguments);
	}

}
