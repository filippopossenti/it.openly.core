package it.openly.core.patterns.observer;

public class StateInfo {
	private int code;
	private Object object;
	private Exception exception;
	
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
