package com.softhinkers.galsen.android;

import java.io.File;

import org.apache.commons.exec.CommandLine;
import org.openqa.selenium.Platform;

import com.softhinkers.galsen.exceptions.ShellCommandException;
import com.softhinkers.galsen.io.ShellCommand;

public class JavaSdk {
	public static String javaHome = null;

	public static String javaHome() {
		if (javaHome == null) {
			javaHome = System.getenv("JAVA_HOME");

			if ((javaHome == null) && (Platform.getCurrent() == Platform.MAC)) {
				try {
					javaHome = ShellCommand.exec(new CommandLine(
							"/usr/libexec/java_home"));
					if (javaHome != null)
						javaHome = javaHome.replaceAll("\\r|\\n", "");
				} catch (ShellCommandException e) {
				}
			}
			if (javaHome == null) {
				javaHome = System.getProperty("java.home");
			}
		}
		return javaHome;
	}

	public static File jarsigner() {
		StringBuffer jarsigner = new StringBuffer();
		jarsigner.append(javaHome());
		jarsigner.append(File.separator);
		jarsigner.append("bin");
		jarsigner.append(File.separator);

		return new File(jarsigner.toString(), "jarsigner"
				+ OS.platformExecutableSuffixExe());
	}

	public static File keytool() {
		StringBuffer keytool = new StringBuffer();
		keytool.append(javaHome());
		keytool.append(File.separator);
		keytool.append("bin");
		keytool.append(File.separator);

		return new File(keytool.toString(), "keytool"
				+ OS.platformExecutableSuffixExe());
	}
}