package com.softhinkers.galsen.android;


import java.util.List;
import java.util.Locale;

import com.softhinkers.galsen.device.DeviceTargetPlatform;
import com.softhinkers.galsen.exceptions.AndroidDeviceException;
import com.softhinkers.galsen.exceptions.AndroidSdkException;
import com.softhinkers.galsen.log.LogEntry;

public abstract interface AndroidDevice {
	public abstract boolean isDeviceReady();

	public abstract Boolean install(AndroidApp paramAndroidApp);

	public abstract boolean isInstalled(String paramString)
			throws AndroidSdkException;

	public abstract boolean isInstalled(AndroidApp paramAndroidApp)
			throws AndroidSdkException;

	public abstract void uninstall(AndroidApp paramAndroidApp)
			throws AndroidSdkException;

	public abstract boolean start(AndroidApp paramAndroidApp)
			throws AndroidSdkException;

	public abstract void forwardPort(int paramInt1, int paramInt2);

	public abstract void clearUserData(AndroidApp paramAndroidApp)
			throws AndroidSdkException;

	public abstract void startGalsen(AndroidApp paramAndroidApp,
			int paramInt) throws AndroidSdkException;

	public abstract boolean isGalsenRunning();

	public abstract int getGalensPort();

	public abstract void kill(AndroidApp paramAndroidApp)
			throws AndroidDeviceException, AndroidSdkException;

	public abstract String getScreenSize();

	public abstract List<LogEntry> getLogs();

	public abstract boolean screenSizeMatches(String paramString);

	public abstract Locale getLocale();

	public abstract DeviceTargetPlatform getTargetPlatform();

	public abstract void runAdbCommand(String paramString);

	public abstract byte[] takeScreenshot() throws AndroidDeviceException;

	public abstract void setVerbose();

	public abstract String getSerial();

	public abstract void inputKeyevent(int paramInt);

	public abstract void invokeActivity(String paramString);

	public abstract void restartADB();
}