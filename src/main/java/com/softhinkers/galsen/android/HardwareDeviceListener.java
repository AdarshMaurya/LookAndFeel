package com.softhinkers.galsen.android;


public abstract interface HardwareDeviceListener {
	public abstract void onDeviceConnected(AndroidDevice paramAndroidDevice);

	public abstract void onDeviceDisconnected(AndroidDevice paramAndroidDevice);
}