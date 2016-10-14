package com.softhinkers.galsen.android;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.Collections;

import com.softhinkers.galsen.exceptions.AndroidSdkException;
import com.softhinkers.galsen.exceptions.GalsenException;

public class AndroidSdk {
	public static final String ANDROID_FOLDER_PREFIX = "android-";
	public static final String ANDROID_HOME = "ANDROID_HOME";

	public static File adb() {
		return new File(platformToolsHome(), "adb"
				+ OS.platformExecutableSuffixExe());
	}

	public static File aapt() throws AndroidSdkException {
		StringBuffer command = new StringBuffer();
		command.append("aapt");
		command.append(OS.platformExecutableSuffixExe());
		File platformToolsAapt = new File(platformToolsHome(),
				command.toString());

		if (platformToolsAapt.isFile()) {
			return platformToolsAapt;
		}

		File buildToolsFolder = buildToolsHome();

		return new File(
				findLatestAndroidPlatformFolder(
						buildToolsFolder,
						"Command 'aapt' was not found inside the Android SDK. Please update to the latest development tools and try again."),
				command.toString());
	}

	public static File android() {
		StringBuffer command = new StringBuffer();
		command.append(toolsHome());

		return new File(toolsHome(), "android"
				+ OS.platformExecutableSuffixBat());
	}

	public static File emulator() {
		return new File(toolsHome(), "emulator"
				+ OS.platformExecutableSuffixExe());
	}

	private static File toolsHome() {
		StringBuffer command = new StringBuffer();
		command.append(androidHome());
		command.append(File.separator);
		command.append("tools");
		command.append(File.separator);
		return new File(command.toString());
	}

	private static File buildToolsHome() {
		StringBuffer command = new StringBuffer();
		command.append(androidHome());
		command.append(File.separator);
		command.append("build-tools");
		command.append(File.separator);

		return new File(command.toString());
	}

	private static File platformToolsHome() {
		StringBuffer command = new StringBuffer();
		command.append(androidHome());
		command.append(File.separator);
		command.append("platform-tools");
		command.append(File.separator);
		return new File(command.toString());
	}

	public static String androidHome() {
		String androidHome = System.getenv("ANDROID_HOME");

		if (androidHome == null) {
			throw new GalsenException(
					"Environment variable 'ANDROID_HOME' was not found!");
		}
		return androidHome;
	}

	public static String androidJar() {
		String platformsRootFolder = androidHome() + File.separator
				+ "platforms";
		File platformsFolder = new File(platformsRootFolder);

		return new File(findLatestAndroidPlatformFolder(platformsFolder,
				"No installed Android APIs have been found."), "android.jar")
				.getAbsolutePath();
	}

	protected static File findLatestAndroidPlatformFolder(File rootFolder,
			String errorMessage) {
		File[] androidApis = rootFolder.listFiles(new AndroidFileFilter());
		if ((androidApis == null) || (androidApis.length == 0)) {
			throw new GalsenException(errorMessage);
		}
		Arrays.sort(androidApis, Collections.reverseOrder());
		return androidApis[0].getAbsoluteFile();
	}

	public static class AndroidFileFilter implements FileFilter {
		public boolean accept(File pathname) {
			String fileName = pathname.getName();

			String regex = "\\d{2}\\.\\d{1}\\.\\d{1}";

			return ((fileName.matches(regex)) || (fileName
					.startsWith("android-")));
		}
	}
}