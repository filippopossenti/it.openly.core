package it.openly.core.exceptions;

public class WrongArgumentsNumberException extends AppRuntimeException {
	private static final long serialVersionUID = -7722372873386598176L;

	public WrongArgumentsNumberException(String message, Object... arguments) {
		super(message, arguments);
	}

	public WrongArgumentsNumberException(String message, Throwable cause, Object... arguments) {
		super(message, cause, arguments);
	}

}
