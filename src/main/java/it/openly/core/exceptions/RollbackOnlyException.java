package it.openly.core.exceptions;

public class RollbackOnlyException extends AppRuntimeException {
	private static final long serialVersionUID = -4978394417377708869L;

	public RollbackOnlyException(String message, Object... arguments) {
		super(message, arguments);
	}

	public RollbackOnlyException(String message, Throwable cause, Object... arguments) {
		super(message, cause, arguments);
	}

}
