package com.softhinkers.galsen.exceptions;

public class AndroidDeviceException extends Exception {
	private static final long serialVersionUID = 5431510243540521939L;

	public AndroidDeviceException(String message) {
		super(message);
	}

	public AndroidDeviceException(Throwable t) {
		super(t);
	}

	public AndroidDeviceException(String message, Throwable t) {
		super(message, t);
	}
}