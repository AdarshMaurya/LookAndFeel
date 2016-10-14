package com.softhinkers.galsen.android;

public class OS {
	public static boolean isWindows() {
		return (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0);
	}

	static String platformExecutableSuffixExe() {
		return ((isWindows()) ? ".exe" : "");
	}

	static String platformExecutableSuffixBat() {
		return ((isWindows()) ? ".bat" : "");
	}
}
