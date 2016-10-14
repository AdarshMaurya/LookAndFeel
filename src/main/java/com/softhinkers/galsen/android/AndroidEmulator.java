package com.softhinkers.galsen.android;


import java.io.File;
import java.util.Locale;
import java.util.Map;

import com.android.ddmlib.IDevice;
import com.softhinkers.galsen.device.DeviceTargetPlatform;
import com.softhinkers.galsen.exceptions.AndroidDeviceException;



public abstract interface AndroidEmulator {
	public static final String TIMEOUT_OPTION = "TIMEOUT";
	public static final String DISPLAY_OPTION = "DISPLAY";
	public static final String EMULATOR_OPTIONS = "OPTIONS";

	public abstract boolean isEmulatorAlreadyExistent()
			throws AndroidDeviceException;

	public abstract boolean isEmulatorStarted() throws AndroidDeviceException;

	public abstract String getAvdName();

	public abstract File getAvdRootFolder();

	public abstract String getScreenSize();

	public abstract DeviceTargetPlatform getTargetPlatform();

	public abstract void start(Locale paramLocale, int paramInt,
			Map<String, Object> paramMap) throws AndroidDeviceException;

	public abstract void stop() throws AndroidDeviceException;

	public abstract Integer getPort();

	public abstract void setIDevice(IDevice paramIDevice);

	public abstract String getSerial();

	public abstract void setSerial(int paramInt);

	public abstract void unlockEmulatorScreen() throws AndroidDeviceException;

	public abstract void setWasStartedBySelendroid(boolean paramBoolean);
}