package com.softhinkers.galsen.android.impl;

import java.util.Locale;
import java.util.logging.Logger;

import com.android.ddmlib.IDevice;
import com.android.ddmlib.RawImage;
import com.softhinkers.galsen.device.DeviceTargetPlatform;

public class DefaultHardwareDevice extends AbstractDevice {
	private static final Logger log;
	private String model;
	private Locale locale;
	private DeviceTargetPlatform targetPlatform;
	private String screenSize;

	public DefaultHardwareDevice(final IDevice device) {
		super(device);
		this.model = null;
		this.locale = null;
		this.targetPlatform = null;
		this.screenSize = null;
	}

	public String getModel() {
		if (this.model == null) {
			this.model = this.getProp("ro.product.model");
		}
		return this.model;
	}

	protected String getProp(final String key) {
		return this.device.getProperty(key);
	}

	public DeviceTargetPlatform getTargetPlatform() {
		if (this.targetPlatform == null) {
			final String version = this.getProp("ro.build.version.sdk");
			this.targetPlatform = DeviceTargetPlatform.fromInt(version);
		}
		return this.targetPlatform;
	}

	public String getScreenSize() {
		if (this.screenSize == null) {
			RawImage screeshot = null;
			try {
				screeshot = this.device.getScreenshot();
				this.screenSize = screeshot.height + "x" + screeshot.width;
			} catch (Exception e) {
				DefaultHardwareDevice.log
						.warning("was not able to determine screensize: "
								+ e.getMessage());
				e.printStackTrace();
			}
		}
		return this.screenSize;
	}

	public Locale getLocale() {
		if (this.locale == null) {
			this.locale = new Locale(this.getProp("persist.sys.language"),
					this.getProp("persist.sys.country"));
		}
		return this.locale;
	}

	public boolean isDeviceReady() {
		return true;
	}

	public String toString() {
		return "HardwareDevice [serial=" + this.serial + ", model="
				+ this.getModel() + ", targetVersion="
				+ this.getTargetPlatform() + "]";
	}

	public String getSerial() {
		return this.serial;
	}

	static {
		log = Logger.getLogger(DefaultHardwareDevice.class.getName());
	}

	@Override
	public int getGalensPort() {
		// TODO
		return 0;
	}
}