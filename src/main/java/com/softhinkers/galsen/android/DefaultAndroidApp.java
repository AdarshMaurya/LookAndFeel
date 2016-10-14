package com.softhinkers.galsen.android;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.exec.CommandLine;

import com.softhinkers.galsen.exceptions.AndroidSdkException;
import com.softhinkers.galsen.exceptions.GalsenException;
import com.softhinkers.galsen.exceptions.ShellCommandException;
import com.softhinkers.galsen.io.ShellCommand;

public class DefaultAndroidApp implements AndroidApp {
	private File apkFile;
	private String mainPackage = null;
	protected String mainActivity = null;
	private String versionName = null;

	public DefaultAndroidApp(File apkFile) {
		this.apkFile = apkFile;
	}

	private String extractApkDetails(String regex)
			throws ShellCommandException, AndroidSdkException {
		CommandLine line = new CommandLine(AndroidSdk.aapt());

		line.addArgument("dump", false);
		line.addArgument("badging", false);
		line.addArgument(this.apkFile.getAbsolutePath(), false);
		String output = ShellCommand.exec(line, 20000L);

		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(output);
		if (matcher.find()) {
			return matcher.group(1);
		}

		return null;
	}

	public String getBasePackage() throws AndroidSdkException {
		if (this.mainPackage == null) {
			try {
				this.mainPackage = extractApkDetails("package: name='(.*?)'");
			} catch (ShellCommandException e) {
				throw new GalsenException("The base package name of the apk "
						+ this.apkFile.getName() + " cannot be extracted.");
			}

		}

		return this.mainPackage;
	}

	public String getMainActivity() throws AndroidSdkException {
		if (this.mainActivity == null) {
			try {
				this.mainActivity = extractApkDetails("launchable-activity: name='(.*?)'");
			} catch (ShellCommandException e) {
				throw new GalsenException("The main activity of the apk "
						+ this.apkFile.getName() + " cannot be extracted.");
			}
		}

		return this.mainActivity;
	}

	public void deleteFileFromWithinApk(String file)
			throws ShellCommandException, AndroidSdkException {
		CommandLine line = new CommandLine(AndroidSdk.aapt());
		line.addArgument("remove", false);
		line.addArgument(this.apkFile.getAbsolutePath(), false);
		line.addArgument(file, false);

		ShellCommand.exec(line, 20000L);
	}

	public String getAbsolutePath() {
		return this.apkFile.getAbsolutePath();
	}

	public String getVersionName() throws AndroidSdkException {
		if (this.versionName == null) {
			try {
				this.versionName = extractApkDetails("versionName='(.*?)'");
			} catch (ShellCommandException e) {
				throw new GalsenException("The versionName of the apk "
						+ this.apkFile.getName() + " cannot be extracted.");
			}
		}

		return this.versionName;
	}

	public String getAppId() throws AndroidSdkException {
		return getBasePackage() + ":" + getVersionName();
	}
}