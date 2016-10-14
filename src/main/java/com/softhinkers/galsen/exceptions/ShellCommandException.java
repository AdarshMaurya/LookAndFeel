package com.softhinkers.galsen.exceptions;

public class ShellCommandException extends Exception {
	private static final long serialVersionUID = 268831360479853361L;

	public ShellCommandException(String message) {
		super(message);
	}

	public ShellCommandException(Throwable t) {
		super(t);
	}

	public ShellCommandException(String message, Throwable t) {
		super(message, t);
	}
}