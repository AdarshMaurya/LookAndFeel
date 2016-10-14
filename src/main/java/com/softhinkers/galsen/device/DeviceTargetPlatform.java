package com.softhinkers.galsen.device;

public enum DeviceTargetPlatform {
	ANDROID10, ANDROID11, ANDROID12, ANDROID13, ANDROID14, ANDROID15, ANDROID16, ANDROID17, ANDROID18, ANDROID19;

	public static final String ANDROID = "ANDROID";
	private String versionNumber;
	private String api;

	public String getSdkFolderName() {
		return name().replace("ANDROID", "android-");
	}

	public static DeviceTargetPlatform fromPlatformVersion(String text) {
		if (text != null) {
			for (DeviceTargetPlatform b : values()) {
				if ((b.name().equals("ANDROID" + text))
						|| (b.name().equals(text))) {
					return b;
				}
			}
		}
		return null;
	}

	public static DeviceTargetPlatform fromInt(String text) {
		if (text != null) {
			for (DeviceTargetPlatform b : values()) {
				if (b.name().equals("ANDROID" + text)) {
					return b;
				}
			}
		}
		return null;
	}

	public String getVersionNumber() {
		return this.versionNumber;
	}

	public String getApi() {
		return this.api;
	}
}