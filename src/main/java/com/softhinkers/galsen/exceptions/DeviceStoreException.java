package com.softhinkers.galsen.exceptions;

public class DeviceStoreException extends Exception {
	private static final long serialVersionUID = 5431510243540521939L;

	public DeviceStoreException(String message) {
		super(message);
	}

	public DeviceStoreException(Throwable t) {
		super(t);
	}

	public DeviceStoreException(String message, Throwable t) {
		super(message, t);
	}
}