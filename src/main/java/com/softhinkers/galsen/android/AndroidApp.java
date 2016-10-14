package com.softhinkers.galsen.android;

import com.softhinkers.galsen.exceptions.AndroidSdkException;
import com.softhinkers.galsen.exceptions.ShellCommandException;

public abstract interface AndroidApp {
	public abstract String getBasePackage() throws AndroidSdkException;

	public abstract String getMainActivity() throws AndroidSdkException;

	public abstract String getVersionName() throws AndroidSdkException;

	public abstract void deleteFileFromWithinApk(String paramString)
			throws ShellCommandException, AndroidSdkException;

	public abstract String getAppId() throws AndroidSdkException;

	public abstract String getAbsolutePath();
}