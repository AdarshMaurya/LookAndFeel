package com.softhinkers.galsen.server.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.softhinkers.galsen.GalsenCapabilities;
import com.softhinkers.galsen.android.AndroidApp;
import com.softhinkers.galsen.android.AndroidDevice;
import com.softhinkers.galsen.android.AndroidEmulator;
import com.softhinkers.galsen.android.AndroidEmulatorPowerStateListener;
import com.softhinkers.galsen.android.DeviceManager;
import com.softhinkers.galsen.android.HardwareDeviceListener;
import com.softhinkers.galsen.android.impl.DefaultAndroidEmulator;
import com.softhinkers.galsen.android.impl.DefaultHardwareDevice;
import com.softhinkers.galsen.device.DeviceTargetPlatform;
import com.softhinkers.galsen.exceptions.AndroidDeviceException;
import com.softhinkers.galsen.exceptions.AndroidSdkException;
import com.softhinkers.galsen.exceptions.DeviceStoreException;
import com.softhinkers.galsen.exceptions.GalsenException;
import com.softhinkers.server.model.impl.DefaultPortFinder;

public class DeviceStore {
	private static final Logger log = Logger.getLogger(DeviceStore.class
			.getName());
	private List<AndroidDevice> devicesInUse = new ArrayList();
	private Map<DeviceTargetPlatform, List<AndroidDevice>> androidDevices = new HashMap();

	private EmulatorPortFinder androidEmulatorPortFinder = null;
	private boolean clearData = true;
	private AndroidEmulatorPowerStateListener emulatorPowerStateListener = null;
	private DeviceManager deviceManager = null;

	public DeviceStore(Integer emulatorPort, DeviceManager deviceManager) {
		this.deviceManager = deviceManager;
		this.androidEmulatorPortFinder = new DefaultPortFinder(emulatorPort,
				Integer.valueOf(emulatorPort.intValue() + 30));
	}

	public DeviceStore(EmulatorPortFinder androidEmulatorPortFinder,
			DeviceManager deviceManager) {
		this.deviceManager = deviceManager;
		this.androidEmulatorPortFinder = androidEmulatorPortFinder;
	}

	public Integer nextEmulatorPort() {
		return this.androidEmulatorPortFinder.next();
	}

	public void release(AndroidDevice device, AndroidApp aut) {
		if (!(this.devicesInUse.contains(device)))
			return;
		try {
			device.kill(aut);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (this.clearData) {
			try {
				device.clearUserData(aut);
			} catch (AndroidSdkException e) {
				e.printStackTrace();
			}
		}
		if (device instanceof AndroidEmulator) {
			AndroidEmulator emulator = (AndroidEmulator) device;
			try {
				emulator.stop();
			} catch (AndroidDeviceException e) {
				log.severe("Failed to stop emulator: " + e.getMessage());
			}
			this.androidEmulatorPortFinder.release(emulator.getPort());
		}
		this.devicesInUse.remove(device);
	}

	void initAndroidDevices(HardwareDeviceListener hardwareDeviceListener,
			boolean shouldKeepAdbAlive) throws AndroidDeviceException {
		this.emulatorPowerStateListener = new DefaultEmulatorPowerStateListener();
		this.deviceManager.initialize(hardwareDeviceListener,
				this.emulatorPowerStateListener);

		List emulators = DefaultAndroidEmulator.listAvailableAvds();
		addEmulators(emulators);

		if (getDevices().isEmpty()) {
			GalsenException e = new GalsenException(
					"No android virtual devices were found. Please start the android tool and create emulators and restart the selendroid-standalone or plugin an Android hardware device via USB.");

			log.warning("Warning: " + e);
		}
	}

	public synchronized void addDevice(AndroidDevice androidDevice)
			throws AndroidDeviceException {
		if (androidDevice == null) {
			log.info("No Android devices were found.");
			return;
		}
		if (androidDevice instanceof AndroidEmulator) {
			throw new AndroidDeviceException(
					"For adding emulator instances please use #addEmulator method.");
		}

		if (androidDevice.isDeviceReady() == true) {
			log.info("Adding: " + androidDevice);
			addDeviceToStore(androidDevice);
		}
	}

	public void addEmulators(List<AndroidEmulator> emulators)
			throws AndroidDeviceException {
		if ((emulators == null) || (emulators.isEmpty())) {
			log.info("No emulators has been found.");
			return;
		}
		for (AndroidEmulator emulator : emulators) {
			log.info("Adding: " + emulator);
			addDeviceToStore((AndroidDevice) emulator);
		}
	}

	protected synchronized void addDeviceToStore(AndroidDevice device)
			throws AndroidDeviceException {
		if (this.androidDevices.containsKey(device.getTargetPlatform())) {
			if (this.androidDevices.get(device.getTargetPlatform()) == null) {
				this.androidDevices.put(device.getTargetPlatform(),
						new ArrayList());
			}
			((List) this.androidDevices.get(device.getTargetPlatform()))
					.add(device);
		} else {
			List devices = new ArrayList();
			devices.add(device);
			this.androidDevices.put(device.getTargetPlatform(), devices);
		}
	}

	public synchronized AndroidDevice findAndroidDevice(
			final GalsenCapabilities caps) throws DeviceStoreException {
		if (caps == null) {
			throw new IllegalArgumentException("Error: capabilities are null");
		}
		if (this.androidDevices.isEmpty()) {
			throw new DeviceStoreException(
					"Fatal Error: Device Store does not contain any Android Device.");
		}
		final String platformVersion = caps.getPlatformVersion();
		List<AndroidDevice> devices = null;
		if (platformVersion == null || platformVersion.isEmpty()) {
			devices = new ArrayList<AndroidDevice>();
			for (final List<AndroidDevice> list : this.androidDevices.values()) {
				devices.addAll(list);
			}
		} else {
			final DeviceTargetPlatform platform = DeviceTargetPlatform
					.fromPlatformVersion(platformVersion);
			devices = this.androidDevices.get(platform);
		}
		if (devices == null) {
			devices = new ArrayList<AndroidDevice>();
		}
		final List<AndroidDevice> potentialMatches = new ArrayList<AndroidDevice>();
		for (final AndroidDevice device : devices) {
			DeviceStore.log
					.info("Evaluating if this device is a match for this session: "
							+ device.toString());
			if (device.screenSizeMatches(caps.getScreenSize())) {
				if (this.devicesInUse.contains(device)) {
					DeviceStore.log.info("Device is in use.");
				} else {
					if (caps.getEmulator() != null
							&& (!caps.getEmulator() || !(device instanceof DefaultAndroidEmulator))
							&& (caps.getEmulator() || !(device instanceof DefaultHardwareDevice))) {
						continue;
					}
					final String serial = caps.getSerial();
					if (serial != null && device.getSerial().equals(serial)) {
						this.devicesInUse.add(device);
						return device;
					}
					if (!(device instanceof DefaultAndroidEmulator)
							|| ((DefaultAndroidEmulator) device)
									.isEmulatorStarted()) {
						this.devicesInUse.add(device);
						return device;
					}
					potentialMatches.add(device);
				}
			}
		}
		if (potentialMatches.size() > 0) {
			DeviceStore.log.info("Using potential match: "
					+ potentialMatches.get(0));
			this.devicesInUse.add(potentialMatches.get(0));
			return potentialMatches.get(0);
		}
		throw new DeviceStoreException(
				"No devices are found. This can happen if the devices are in use or no device screen matches the required capabilities.");
	}

	private boolean isEmulatorSwitchedOff(AndroidDevice device)
			throws DeviceStoreException {
		if (device instanceof AndroidEmulator) {
			try {
				return (!(((AndroidEmulator) device).isEmulatorStarted()));
			} catch (AndroidDeviceException e) {
				throw new DeviceStoreException(e);
			}
		}
		return true;
	}

	public List<AndroidDevice> getDevices() {
		List devices = new ArrayList();
		for (Map.Entry entry : this.androidDevices.entrySet()) {
			devices.addAll((Collection) entry.getValue());
		}
		return devices;
	}

	List<AndroidDevice> getDevicesInUse() {
		return this.devicesInUse;
	}

	Map<DeviceTargetPlatform, List<AndroidDevice>> getDevicesList() {
		return this.androidDevices;
	}

	public void removeAndroidDevice(AndroidDevice device)
			throws DeviceStoreException {
		if (device == null) {
			return;
		}
		boolean hardwareDevice = device instanceof DefaultHardwareDevice;
		if (!(hardwareDevice)) {
			throw new DeviceStoreException(
					"Only devices of type 'DefaultHardwareDevice' can be removed.");
		}

		release(device, null);
		DeviceTargetPlatform apiLevel = device.getTargetPlatform();
		if (this.androidDevices.containsKey(apiLevel)) {
			log.info("Removing: " + device);
			((List) this.androidDevices.get(apiLevel)).remove(device);
			if (((List) this.androidDevices.get(apiLevel)).isEmpty())
				this.androidDevices.remove(apiLevel);
		} else {
			log.warning("The target platform version of the device is not found in device store.");
			log.warning("The device was propably already removed.");
		}
	}

	public void setClearData(boolean clearData) {
		this.clearData = clearData;
	}

	class DefaultEmulatorPowerStateListener implements
			AndroidEmulatorPowerStateListener {
		public void onDeviceStarted(String avdName, String serial) {
			AndroidEmulator emulator = findEmulator(avdName);
			if (emulator != null) {
				Integer port = Integer.valueOf(Integer.parseInt(serial.replace(
						"emulator-", "")));
				emulator.setSerial(port.intValue());
				emulator.setWasStartedBySelendroid(false);
			}
		}

		AndroidEmulator findEmulator(String avdName) {
			for (AndroidDevice device : DeviceStore.this.getDevices()) {
				if (device instanceof AndroidEmulator) {
					AndroidEmulator emulator = (AndroidEmulator) device;
					if (avdName.equals(emulator.getAvdName())) {
						return emulator;
					}
				}
			}
			return null;
		}

		public void onDeviceStopped(String avdName) {
		}
	}
}