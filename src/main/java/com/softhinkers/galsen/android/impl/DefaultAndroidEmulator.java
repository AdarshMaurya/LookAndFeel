package com.softhinkers.galsen.android.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.android.ddmlib.IDevice;
import com.beust.jcommander.internal.Lists;
import com.softhinkers.galsen.android.AndroidEmulator;
import com.softhinkers.galsen.android.AndroidSdk;
import com.softhinkers.galsen.android.TelnetClient;
import com.softhinkers.galsen.device.DeviceTargetPlatform;
import com.softhinkers.galsen.exceptions.AndroidDeviceException;
import com.softhinkers.galsen.exceptions.GalsenException;
import com.softhinkers.galsen.exceptions.ShellCommandException;
import com.softhinkers.galsen.io.ShellCommand;

public class DefaultAndroidEmulator extends AbstractDevice implements
		AndroidEmulator {
	private static final String EMULATOR_SERIAL_PREFIX = "emulator-";
	private static final Logger log = Logger
			.getLogger(DefaultAndroidEmulator.class.getName());
	public static final String ANDROID_EMULATOR_HARDWARE_CONFIG = "hardware-qemu.ini";
	public static final String FILE_LOCKING_SUFIX = ".lock";
	private String screenSize;
	private DeviceTargetPlatform targetPlatform;
	private String avdName;
	private File avdRootFolder;
	private Locale locale = null;
	private boolean wasStartedBySelendroid;

	protected DefaultAndroidEmulator() {
		this.wasStartedBySelendroid = Boolean.FALSE.booleanValue();
	}

	public DefaultAndroidEmulator(String avdName, String abi,
			String screenSize, String target, File avdFilePath) {
		this.avdName = avdName;
		this.screenSize = screenSize;
		this.avdRootFolder = avdFilePath;
		this.targetPlatform = DeviceTargetPlatform.fromInt(target);
		this.wasStartedBySelendroid = !this.isEmulatorStarted();
	}

	public File getAvdRootFolder() {
		return this.avdRootFolder;
	}

	public String getScreenSize() {
		return this.screenSize;
	}

	public DeviceTargetPlatform getTargetPlatform() {
		return this.targetPlatform;
	}

	public boolean isEmulatorAlreadyExistent() {
		File emulatorFolder = new File(FileUtils.getUserDirectory(),
				File.separator + ".android" + File.separator + "avd"
						+ File.separator + this.getAvdName() + ".avd");
		return emulatorFolder.exists();
	}

	public String getAvdName() {
		return this.avdName;
	}

	public static List<AndroidEmulator> listAvailableAvds()
			throws AndroidDeviceException {
		List avds = Lists.newArrayList();
		CommandLine cmd = new CommandLine(AndroidSdk.android());
		cmd.addArgument("list", false);
		cmd.addArgument("avds", false);
		String output = null;

		try {
			output = ShellCommand.exec(cmd, 20000L);
		} catch (ShellCommandException arg12) {
			throw new AndroidDeviceException(arg12);
		}

		Map startedDevices = mapDeviceNamesToSerial();
		String[] avdsOutput = StringUtils.splitByWholeSeparator(output,
				"---------");
		if (avdsOutput != null && avdsOutput.length > 0) {
			for (int i = 0; i < avdsOutput.length; ++i) {
				if (avdsOutput[i].contains("Name:")) {
					String element = avdsOutput[i];
					String avdName = extractValue("Name: (.*?)$", element);
					String abi = extractValue("ABI: (.*?)$", element);
					String screenSize = extractValue("Skin: (.*?)$", element);
					String target = extractValue("\\(API level (.*?)\\)",
							element);
					File avdFilePath = new File(extractValue("Path: (.*?)$",
							element));
					DefaultAndroidEmulator emulator = new DefaultAndroidEmulator(
							avdName, abi, screenSize, target, avdFilePath);
					if (startedDevices.containsKey(avdName)) {
						emulator.setSerial(((Integer) startedDevices
								.get(avdName)).intValue());
					}

					avds.add(emulator);
				}
			}
		}

		return avds;
	}

	private static Map<String, Integer> mapDeviceNamesToSerial() {
		HashMap mapping = new HashMap();
		CommandLine command = new CommandLine(AndroidSdk.adb());
		command.addArgument("devices");

		Scanner scanner;
		try {
			scanner = new Scanner(ShellCommand.exec(command));
		} catch (ShellCommandException arg32) {
			return mapping;
		}

		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			Pattern pattern = Pattern.compile("emulator-\\d\\d\\d\\d");
			Matcher matcher = pattern.matcher(line);
			if (matcher.find()) {
				String serial = matcher.group(0);
				Integer port = Integer.valueOf(serial.replaceAll("emulator-",
						""));
				TelnetClient client = null;

				try {
					client = new TelnetClient(port);
					String socket = client.sendCommand("avd name");
					mapping.put(socket, port);
				} catch (AndroidDeviceException arg31) {
					;
				} finally {
					if (client != null) {
						client.close();
					}

				}

				Socket socket1 = null;
				PrintWriter out = null;
				BufferedReader in = null;

				try {
					socket1 = new Socket("127.0.0.1", port.intValue());
					out = new PrintWriter(socket1.getOutputStream(), true);
					in = new BufferedReader(new InputStreamReader(
							socket1.getInputStream()));
					if (in.readLine() == null) {
						throw new AndroidDeviceException("error");
					}

					out.write("avd name\r\n");
					out.flush();
					in.readLine();
					String e = in.readLine();
					mapping.put(e, port);
				} catch (Exception arg29) {
					;
				} finally {
					try {
						out.close();
						in.close();
						socket1.close();
					} catch (Exception arg28) {
						;
					}

				}
			}
		}

		scanner.close();
		return mapping;
	}

	public boolean isEmulatorStarted() {
		File lockedEmulatorHardwareConfig = new File(this.avdRootFolder,
				"hardware-qemu.ini.lock");
		return lockedEmulatorHardwareConfig.exists();
	}

	public String toString() {
		return "AndroidEmulator [screenSize=" + this.screenSize
				+ ", targetPlatform=" + this.targetPlatform + ", serial="
				+ this.serial + ", avdName=" + this.avdName + "]";
	}

	public void setSerial(int port) {
		this.port = Integer.valueOf(port);
		this.serial = "emulator-" + port;
	}

	public Integer getPort() {
		return this.isSerialConfigured() ? Integer.valueOf(Integer
				.parseInt(this.serial.replace("emulator-", ""))) : null;
	}

	public void start(Locale locale, int emulatorPort,
			Map<String, Object> options) throws AndroidDeviceException {
		if (this.isEmulatorStarted()) {
			throw new GalsenException(
					"Error - Android emulator is already started " + this);
		} else {
			Long timeout = null;
			String emulatorOptions = null;
			String display = null;
			if (options != null) {
				if (options.containsKey("TIMEOUT")) {
					timeout = (Long) options.get("TIMEOUT");
				}

				if (options.containsKey("DISPLAY")) {
					display = (String) options.get("DISPLAY");
				}

				if (options.containsKey("OPTIONS")) {
					emulatorOptions = (String) options.get("OPTIONS");
				}
			}

			if (display != null) {
				log.info("Using display " + display
						+ " for running the emulator");
			}

			if (timeout == null) {
				timeout = Long.valueOf(120000L);
			}

			log.info("Using timeout of \'" + timeout.longValue() / 1000L
					+ "\' seconds to start the emulator.");
			this.locale = locale;
			CommandLine cmd = new CommandLine(AndroidSdk.emulator());
			cmd.addArgument("-no-snapshot-save", false);
			cmd.addArgument("-avd", false);
			cmd.addArgument(this.avdName, false);
			cmd.addArgument("-port", false);
			cmd.addArgument(String.valueOf(emulatorPort), false);
			if (locale != null) {
				cmd.addArgument("-prop", false);
				cmd.addArgument("persist.sys.language=" + locale.getLanguage(),
						false);
				cmd.addArgument("-prop", false);
				cmd.addArgument("persist.sys.country=" + locale.getCountry(),
						false);
			}

			if (emulatorOptions != null && !emulatorOptions.isEmpty()) {
				cmd.addArgument(emulatorOptions, false);
			}

			long start = System.currentTimeMillis();
			long timemoutEnd = start + timeout.longValue();

			try {
				ShellCommand.execAsync(display, cmd);
			} catch (ShellCommandException arg19) {
				throw new GalsenException("unable to start the emulator: "
						+ this);
			}

			this.setSerial(emulatorPort);
			Boolean adbKillServerAttempted = Boolean.valueOf(false);

			while (!this.isDeviceReady()) {
				if (!adbKillServerAttempted.booleanValue()
						&& System.currentTimeMillis() - start > 10000L) {
					CommandLine e = new CommandLine(AndroidSdk.adb());
					e.addArgument("devices", false);
					String devices = "";

					try {
						devices = ShellCommand.exec(e, 20000L);
					} catch (ShellCommandException arg17) {
						;
					}

					if (!devices.contains(String.valueOf(emulatorPort))) {
						CommandLine resetAdb = new CommandLine(AndroidSdk.adb());
						resetAdb.addArgument("kill-server", false);

						try {
							ShellCommand.exec(resetAdb, 20000L);
						} catch (ShellCommandException arg16) {
							throw new GalsenException(
									"unable to kill the adb server");
						}
					}

					adbKillServerAttempted = Boolean.valueOf(true);
				}

				if (timemoutEnd < System.currentTimeMillis()) {
					throw new AndroidDeviceException("The emulator with avd \'"
							+ this.getAvdName() + "\' was not started after "
							+ (System.currentTimeMillis() - start) / 1000L
							+ " seconds.");
				}

				try {
					Thread.sleep(2000L);
				} catch (InterruptedException arg18) {
					;
				}
			}

			log.info("Emulator start took: "
					+ (System.currentTimeMillis() - start) / 1000L + " seconds");
			log.info("Please have in mind, starting an emulator takes usually about 45 seconds.");
			this.unlockEmulatorScreen();
			this.waitForLauncherToComplete();
			this.allAppsGridView();
			this.waitForLauncherToComplete();
			this.setWasStartedBySelendroid(true);
		}
	}

	public void unlockEmulatorScreen() throws AndroidDeviceException {
		CommandLine event82 = new CommandLine(AndroidSdk.adb());
		if (this.isSerialConfigured()) {
			event82.addArgument("-s", false);
			event82.addArgument(this.serial, false);
		}

		event82.addArgument("shell", false);
		event82.addArgument("input", false);
		event82.addArgument("keyevent", false);
		event82.addArgument("82", false);

		try {
			ShellCommand.exec(event82, 20000L);
		} catch (ShellCommandException arg4) {
			throw new AndroidDeviceException(arg4);
		}

		CommandLine event4 = new CommandLine(AndroidSdk.adb());
		if (this.isSerialConfigured()) {
			event4.addArgument("-s", false);
			event4.addArgument(this.serial, false);
		}

		event4.addArgument("shell", false);
		event4.addArgument("input", false);
		event4.addArgument("keyevent", false);
		event4.addArgument("4", false);

		try {
			ShellCommand.exec(event4, 20000L);
		} catch (ShellCommandException arg3) {
			throw new AndroidDeviceException(arg3);
		}
	}

	private void waitForLauncherToComplete() throws AndroidDeviceException {
		this.waitForLauncherToComplete(Boolean.valueOf(true));
	}

	private void waitForLauncherToComplete(Boolean delay)
			throws AndroidDeviceException {
		CommandLine event = new CommandLine(AndroidSdk.adb());
		if (this.isSerialConfigured()) {
			event.addArgument("-s", false);
			event.addArgument(this.serial, false);
		}

		event.addArgument("shell", false);
		event.addArgument("ps", false);
		String homeScreenLaunched = null;

		try {
			homeScreenLaunched = ShellCommand.exec(event, 20000L);
		} catch (ShellCommandException arg6) {
			throw new AndroidDeviceException(arg6);
		}

		if (homeScreenLaunched != null
				&& homeScreenLaunched.contains("S com.android.launcher")) {
			if (!delay.booleanValue()) {
				return;
			}
		} else {
			try {
				Thread.sleep(500L);
			} catch (InterruptedException arg5) {
				throw new RuntimeException(arg5);
			}

			this.waitForLauncherToComplete(Boolean.valueOf(true));
		}

		try {
			Thread.sleep(1000L);
		} catch (InterruptedException arg4) {
			throw new RuntimeException(arg4);
		}

		this.waitForLauncherToComplete(Boolean.valueOf(false));
	}

	private void allAppsGridView() throws AndroidDeviceException {
		String[] dimensions = this.screenSize.split("x");
		int x = Integer.parseInt(dimensions[0]);
		int y = Integer.parseInt(dimensions[1]);
		if (x > y) {
			y /= 2;
			x -= 30;
		} else {
			x /= 2;
			y -= 30;
		}

		ArrayList coordinates = new ArrayList();
		coordinates.add("3 0 " + x);
		coordinates.add("3 1 " + y);
		coordinates.add("1 330 1");
		coordinates.add("0 0 0");
		coordinates.add("1 330 0");
		coordinates.add("0 0 0");
		Iterator e = coordinates.iterator();

		while (e.hasNext()) {
			String coordinate = (String) e.next();
			CommandLine event1 = new CommandLine(AndroidSdk.adb());
			if (this.isSerialConfigured()) {
				event1.addArgument("-s", false);
				event1.addArgument(this.serial, false);
			}

			event1.addArgument("shell", false);
			event1.addArgument("sendevent", false);
			event1.addArgument("dev/input/event0", false);
			event1.addArgument(coordinate, false);

			try {
				ShellCommand.exec(event1);
			} catch (ShellCommandException arg9) {
				throw new AndroidDeviceException(arg9);
			}
		}

		try {
			Thread.sleep(750L);
		} catch (InterruptedException arg8) {
			throw new RuntimeException(arg8);
		}
	}

	private void stopEmulator() throws AndroidDeviceException {
		TelnetClient client = null;

		try {
			client = new TelnetClient(this.getPort());
			client.sendQuietly("kill");
		} catch (AndroidDeviceException arg5) {
			;
		} finally {
			if (client != null) {
				client.close();
			}

		}

	}

	public void stop() throws AndroidDeviceException {
		if (this.wasStartedBySelendroid) {
			this.stopEmulator();
			Boolean killed = Boolean.valueOf(false);

			while (this.isEmulatorStarted()) {
				log.info("emulator still running, sleeping 0.5, waiting for it to release the lock");

				try {
					Thread.sleep(500L);
				} catch (InterruptedException arg3) {
					throw new RuntimeException(arg3);
				}

				if (!killed.booleanValue()) {
					try {
						this.stopEmulator();
					} catch (AndroidDeviceException arg2) {
						killed = Boolean.valueOf(true);
					}
				}
			}
		}

	}

	public Locale getLocale() {
		return this.locale;
	}

	public void setIDevice(IDevice iDevice) {
		super.device = iDevice;
	}

	public String getSerial() {
		return this.serial;
	}

	public void setWasStartedBySelendroid(boolean wasStartedBySelendroid) {
		this.wasStartedBySelendroid = wasStartedBySelendroid;
	}
}