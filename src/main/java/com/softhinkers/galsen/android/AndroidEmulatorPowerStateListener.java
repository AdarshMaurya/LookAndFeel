package com.softhinkers.galsen.android;

public abstract interface AndroidEmulatorPowerStateListener {
	public abstract void onDeviceStarted(String paramString1,
			String paramString2);

	public abstract void onDeviceStopped(String paramString);
}