package com.softhinkers.galsen.exceptions;

public class GalsenException extends RuntimeException {
	private static final long serialVersionUID = -1592305571101012890L;

	public GalsenException(String message) {
		super(message);
	}

	public GalsenException(Throwable t) {
		super(t);
	}

	public GalsenException(String message, Throwable t) {
		super(message, t);
	}
}