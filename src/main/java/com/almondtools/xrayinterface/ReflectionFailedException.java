package com.almondtools.xrayinterface;

public class ReflectionFailedException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ReflectionFailedException() {
	}

	public ReflectionFailedException(String message, Throwable cause) {
		super(message, cause);
	}

	public ReflectionFailedException(String message) {
		super(message);
	}

	public ReflectionFailedException(Throwable cause) {
		super(cause);
	}

}
