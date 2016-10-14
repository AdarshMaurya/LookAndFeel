package com.softhinkers.galsen.android.impl;

import java.io.File;

import com.softhinkers.galsen.android.DefaultAndroidApp;

public class MultiActivityAndroidApp extends DefaultAndroidApp {
	public MultiActivityAndroidApp(DefaultAndroidApp app) {
		super(new File(app.getAbsolutePath()));
	}

	public void setMainActivity(String mainActivity) {
		this.mainActivity = mainActivity;
	}
}