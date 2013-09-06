package net.gmx.nosefish.fishysigns.exception;

public class UnsupportedActivatorException extends RuntimeException {
	private static final long serialVersionUID = -7915817782282176585L;

	public UnsupportedActivatorException() {
		super();
	}

	public UnsupportedActivatorException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnsupportedActivatorException(String message) {
		super(message);
	}

	public UnsupportedActivatorException(Throwable cause) {
		super(cause);
	}
}
