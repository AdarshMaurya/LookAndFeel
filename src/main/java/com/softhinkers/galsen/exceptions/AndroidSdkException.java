package com.softhinkers.galsen.exceptions;

public class AndroidSdkException extends Exception {
	private static final long serialVersionUID = 5431510243540521939L;

	public AndroidSdkException(String message) {
		super(message);
	}

	public AndroidSdkException(Throwable t) {
		super(t);
	}

	public AndroidSdkException(String message, Throwable t) {
		super(message, t);
	}

}
