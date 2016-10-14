package com.softhinkers.galsen.builder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

public class AndroidDriverAPKBuilder {
	private static final String PREBUILD_WEBVIEW_APP_PATH_PREFIX = "/prebuild/android-driver-app-";

	public File extractAndroidDriverAPK() {
//		InputStream is = AndroidDriverAPKBuilder.class
//				.getResourceAsStream("/prebuild/android-driver-app-"
//						+ GalsenServerBuilder.getJarVersionNumber() + ".apk");
		
		InputStream is = AndroidDriverAPKBuilder.class
			.getResourceAsStream("prebuild/android-driver-app-"
					+"0.11.0" + ".apk");		
		try {
			File tmpAndroidDriver = File.createTempFile("android-driver",
					".apk");
			IOUtils.copy(is, new FileOutputStream(tmpAndroidDriver));
			IOUtils.closeQuietly(is);
			return tmpAndroidDriver;
		} catch (IOException ioe) {
			throw new RuntimeException(ioe);
		}
	}
}
