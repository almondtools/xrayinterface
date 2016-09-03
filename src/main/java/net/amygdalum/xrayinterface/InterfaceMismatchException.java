package net.amygdalum.xrayinterface;

public class InterfaceMismatchException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public InterfaceMismatchException() {
	}

	public InterfaceMismatchException(String message, Throwable cause) {
		super(message, cause);
	}

	public InterfaceMismatchException(String message) {
		super(message);
	}

	public InterfaceMismatchException(Throwable cause) {
		super(cause);
	}

}
