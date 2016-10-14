package com.softhinkers.galsen.server;

import org.json.JSONArray;


public abstract interface ServerDetails {
	public abstract String getServerVersion();

	public abstract String getCpuArch();

	public abstract String getOsName();

	public abstract String getOsVersion();

	public abstract JSONArray getSupportedApps();

	public abstract JSONArray getSupportedDevices();
}