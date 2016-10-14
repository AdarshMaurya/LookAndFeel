package com.softhinkers.galsen.android.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.Log;
import com.softhinkers.galsen.android.AndroidDevice;
import com.softhinkers.galsen.android.AndroidEmulatorPowerStateListener;
import com.softhinkers.galsen.android.DeviceManager;
import com.softhinkers.galsen.android.HardwareDeviceListener;
import com.softhinkers.galsen.android.TelnetClient;
import com.softhinkers.galsen.exceptions.AndroidDeviceException;

public class DefaultDeviceManager extends Thread implements
		AndroidDebugBridge.IDeviceChangeListener, DeviceManager {
	private static final Logger log;
	private String adbPath;
	private List<HardwareDeviceListener> deviceListeners;
	private List<AndroidEmulatorPowerStateListener> emulatorPowerStateListener;
	private Map<IDevice, DefaultHardwareDevice> connectedDevices;
	private Map<String, IDevice> virtualDevices;
	private AndroidDebugBridge bridge;
	private boolean shouldKeepAdbAlive;

	public DefaultDeviceManager(final String adbPath,
			final boolean shouldKeepAdbAlive) {
		this.deviceListeners = new ArrayList<HardwareDeviceListener>();
		this.emulatorPowerStateListener = new ArrayList<AndroidEmulatorPowerStateListener>();
		this.connectedDevices = new HashMap<IDevice, DefaultHardwareDevice>();
		this.virtualDevices = new HashMap<String, IDevice>();
		this.adbPath = adbPath;
		this.shouldKeepAdbAlive = shouldKeepAdbAlive;
	}

	protected void initializeAdbConnection() {
		try {
			AndroidDebugBridge.init(false);
		} catch (IllegalStateException e) {
			if (!(this.shouldKeepAdbAlive)) {
				e.printStackTrace();
				Log.e("The IllegalStateException is not a show stopper. It has been handled. This is just debug spew. Please proceed.",
						e);
			}

		}

		this.bridge = AndroidDebugBridge.getBridge();

		if (this.bridge == null) {
			this.bridge = AndroidDebugBridge.createBridge(this.adbPath, false);
		}
		IDevice[] devices = this.bridge.getDevices();

		AndroidDebugBridge.addDeviceChangeListener(this);

		if (devices.length > 0) {
			for (int i = 0; i < devices.length; ++i) {
				deviceConnected(devices[i]);
				log.info("my devices: " + devices[i].getAvdName());
			}
		} else {
			long timeout = System.currentTimeMillis() + 2000L;
			while (((devices = this.bridge.getDevices()).length == 0)
					&& (System.currentTimeMillis() < timeout)) {
				try {
					Thread.sleep(50L);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
			if (devices.length > 0)
				for (int i = 0; i < devices.length; ++i) {
					deviceConnected(devices[i]);
					log.info("my devices: " + devices[i].getAvdName());
				}
		}
	}

	public void shutdown() {
		DefaultDeviceManager.log
				.info("Notifying device listener about shutdown");
		for (final HardwareDeviceListener listener : this.deviceListeners) {
			for (final AndroidDevice device : this.connectedDevices.values()) {
				listener.onDeviceDisconnected((AndroidDevice) this.connectedDevices
						.get(device));
			}
		}
		DefaultDeviceManager.log
				.info("Removing Device Manager listener from ADB");
		AndroidDebugBridge
				.removeDeviceChangeListener((AndroidDebugBridge.IDeviceChangeListener) this);
		if (!this.shouldKeepAdbAlive) {
			AndroidDebugBridge.terminate();
		}
		DefaultDeviceManager.log.info("stopping Device Manager");
	}

	public void deviceChanged(final IDevice device, final int changeMask) {
		if (4 == changeMask) {
			if (!device.isEmulator()) {
				for (final HardwareDeviceListener listener : this.deviceListeners) {
					listener.onDeviceConnected((AndroidDevice) this.connectedDevices
							.get(device));
				}
			}
		}
	}

	public void deviceConnected(final IDevice device) {
		if (device == null) {
			return;
		}
		if (device.isEmulator()) {
			final String serial = device.getSerialNumber();
			final Integer port = Integer.parseInt(serial.replace("emulator-",
					""));
			String avdName = null;
			TelnetClient client = null;
			try {
				client = new TelnetClient(port);
				avdName = client.sendCommand("avd name");
			} catch (AndroidDeviceException e) {
			} finally {
				if (client != null) {
					client.close();
				}
			}
			this.virtualDevices.put(avdName, device);
			for (final AndroidEmulatorPowerStateListener listener : this.emulatorPowerStateListener) {
				listener.onDeviceStarted(avdName, device.getSerialNumber());
			}
		} else {
			this.connectedDevices
					.put(device, new DefaultHardwareDevice(device));
			for (final HardwareDeviceListener listener2 : this.deviceListeners) {
				listener2
						.onDeviceConnected((AndroidDevice) this.connectedDevices
								.get(device));
			}
		}
	}

	public void deviceDisconnected(final IDevice device) {
		if (device == null) {
			return;
		}
		if (device.isEmulator()) {
			this.virtualDevices.remove(device.getAvdName());
			for (final AndroidEmulatorPowerStateListener listener : this.emulatorPowerStateListener) {
				listener.onDeviceStopped(device.getSerialNumber());
			}
		} else if (this.connectedDevices.containsKey(device)) {
			for (final HardwareDeviceListener listener2 : this.deviceListeners) {
				listener2
						.onDeviceDisconnected((AndroidDevice) this.connectedDevices
								.get(device));
			}
			this.connectedDevices.remove(device);
		}
	}

	public void registerListener(final HardwareDeviceListener deviceListener) {
		this.deviceListeners.add(deviceListener);
	}

	public void unregisterListener(final HardwareDeviceListener deviceListener) {
		if (this.deviceListeners.contains(deviceListener)) {
			this.deviceListeners.remove(deviceListener);
		}
	}

	public void registerListener(
			final AndroidEmulatorPowerStateListener deviceListener) {
		this.emulatorPowerStateListener.add(deviceListener);
	}

	public void unregisterListener(
			final AndroidEmulatorPowerStateListener deviceListener) {
		if (this.emulatorPowerStateListener.contains(deviceListener)) {
			this.emulatorPowerStateListener.remove(deviceListener);
		}
	}

	public void initialize(
			final HardwareDeviceListener defaultHardwareListener,
			final AndroidEmulatorPowerStateListener emulatorListener) {
		this.registerListener(defaultHardwareListener);
		this.registerListener(emulatorListener);
		this.initializeAdbConnection();
	}

	public IDevice getVirtualDevice(final String avdName) {
		return this.virtualDevices.get(avdName);
	}

	static {
		log = Logger.getLogger(DefaultDeviceManager.class.getName());
	}
}