package com.softhinkers.galsen.server;

public abstract interface Response {
	public abstract String getSessionId();

	public abstract String render();
}