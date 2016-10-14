package com.softhinkers.galsen.android;

import com.android.ddmlib.IDevice;


public abstract interface DeviceManager {
	public abstract void initialize(
			HardwareDeviceListener paramHardwareDeviceListener,
			AndroidEmulatorPowerStateListener paramAndroidEmulatorPowerStateListener);

	public abstract void registerListener(
			HardwareDeviceListener paramHardwareDeviceListener);

	public abstract void unregisterListener(
			HardwareDeviceListener paramHardwareDeviceListener);

	public abstract void registerListener(
			AndroidEmulatorPowerStateListener paramAndroidEmulatorPowerStateListener);

	public abstract void unregisterListener(
			AndroidEmulatorPowerStateListener paramAndroidEmulatorPowerStateListener);

	public abstract void shutdown();

	public abstract IDevice getVirtualDevice(String paramString);
}