package com.softhinkers.galsen.server.model;



import java.util.logging.Logger;

import com.softhinkers.galsen.android.AndroidDevice;
import com.softhinkers.galsen.android.HardwareDeviceListener;
import com.softhinkers.galsen.exceptions.AndroidDeviceException;
import com.softhinkers.galsen.exceptions.DeviceStoreException;

public class DefaultHardwareDeviceListener implements HardwareDeviceListener {
	private static final Logger log = Logger
			.getLogger(DefaultHardwareDeviceListener.class.getName());
	private DeviceStore store = null;
	private GalsenStandaloneDriver driver;

	public DefaultHardwareDeviceListener(DeviceStore store,
			GalsenStandaloneDriver driver) {
		this.store = store;
		this.driver = driver;
	}

	public void onDeviceConnected(AndroidDevice device) {
		try {
			this.store.addDevice(device);
		} catch (AndroidDeviceException e) {
			log.info(e.getMessage());
		}
	}

	public void onDeviceDisconnected(AndroidDevice device) {
		try {
			ActiveSession session = this.driver.findActiveSession(device);
			if (session != null) {
				session.invalidate();
			}

			this.store.removeAndroidDevice(device);
		} catch (DeviceStoreException e) {
			log.severe("The device cannot be removed: " + e.getMessage());
		}
	}
}