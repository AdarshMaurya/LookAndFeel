package com.softhinkers.galsen.server.model;

public abstract interface EmulatorPortFinder {
	public abstract Integer next();

	public abstract void release(Integer paramInteger);
}