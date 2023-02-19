package it.openly.core.patterns.observer;

public class StateInfo {
	private final int code;
	private final Object object;
	private final Exception exception;
	
	public StateInfo(int code, Object object, Exception exception) {
		this.code = code;
		this.object = object;
		this.exception = exception;
	}
	
	public int getCode() {
		return code;
	}
	
	public Object getObject() {
		return object;
	}
	
	public Exception getException() {
		return exception;
	}
}
