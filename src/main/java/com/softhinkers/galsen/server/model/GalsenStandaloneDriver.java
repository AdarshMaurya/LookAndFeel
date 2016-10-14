package com.softhinkers.galsen.server.model;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Logger;

import org.jboss.netty.handler.codec.http.HttpMethod;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import com.beust.jcommander.internal.Lists;
import com.google.common.base.Function;
import com.softhinkers.galsen.GalsenCapabilities;
import com.softhinkers.galsen.GalsenConfiguration;
import com.softhinkers.galsen.android.AndroidApp;
import com.softhinkers.galsen.android.AndroidDevice;
import com.softhinkers.galsen.android.AndroidEmulator;
import com.softhinkers.galsen.android.AndroidSdk;
import com.softhinkers.galsen.android.DefaultAndroidApp;
import com.softhinkers.galsen.android.DeviceManager;
import com.softhinkers.galsen.android.impl.DefaultAndroidEmulator;
import com.softhinkers.galsen.android.impl.DefaultDeviceManager;
import com.softhinkers.galsen.android.impl.DefaultHardwareDevice;
import com.softhinkers.galsen.android.impl.MultiActivityAndroidApp;
import com.softhinkers.galsen.builder.AndroidDriverAPKBuilder;
import com.softhinkers.galsen.builder.GalsenServerBuilder;
import com.softhinkers.galsen.exceptions.AndroidDeviceException;
import com.softhinkers.galsen.exceptions.AndroidSdkException;
import com.softhinkers.galsen.exceptions.DeviceStoreException;
import com.softhinkers.galsen.exceptions.GalsenException;
import com.softhinkers.galsen.exceptions.ShellCommandException;
import com.softhinkers.galsen.server.ServerDetails;
import com.softhinkers.galsen.server.util.HttpClientUtil;

public class GalsenStandaloneDriver implements ServerDetails {

	public static final String WD_RESP_KEY_VALUE = "value";
	public static final String WD_RESP_KEY_STATUS = "status";
	public static final String WD_RESP_KEY_SESSION_ID = "sessionId";

	private static int galsenServerPort = 38080;
	private static final Logger log = Logger
			.getLogger(GalsenStandaloneDriver.class.getName());
	private Map<String, AndroidApp> appsStore = new HashMap();
	private Map<String, AndroidApp> galsenServers = new HashMap();
	private Map<String, ActiveSession> sessions = new HashMap();
	private DeviceStore deviceStore = null;
	private GalsenServerBuilder galsenApkBuilder = null;
	private AndroidDriverAPKBuilder androidDriverAPKBuilder = null;
	private GalsenConfiguration serverConfiguration = null;
	private DeviceManager deviceManager;

	public GalsenStandaloneDriver(GalsenConfiguration serverConfiguration)
			throws AndroidSdkException, AndroidDeviceException {

		this.serverConfiguration = serverConfiguration;
		this.galsenApkBuilder = new GalsenServerBuilder(serverConfiguration);
		this.androidDriverAPKBuilder = new AndroidDriverAPKBuilder();
		galsenServerPort = serverConfiguration.getGalsenServerPort();
		initApplicationsUnderTest(serverConfiguration);
		initAndroidDevices();
		this.deviceStore.setClearData(!(serverConfiguration.isNoClearData()));
	}

	GalsenStandaloneDriver(GalsenServerBuilder builder,
			DeviceManager deviceManager,
			AndroidDriverAPKBuilder androidDriverAPKBuilder) {
		this.galsenApkBuilder = builder;
		this.deviceManager = deviceManager;
		this.androidDriverAPKBuilder = androidDriverAPKBuilder;
	}

	void initApplicationsUnderTest(GalsenConfiguration serverConfiguration)
			throws AndroidSdkException {
		if (serverConfiguration == null) {
			throw new GalsenException(
					"Configuration error - serverConfiguration can't be null.");
		}
		this.serverConfiguration = serverConfiguration;

		for (String appPath : serverConfiguration.getSupportedApps()) {
			File file = new File(appPath);
			if (file.exists()) {
				AndroidApp app = null;
				try {
					app = this.galsenApkBuilder.resignApp(file);
				} catch (ShellCommandException e1) {
					throw new SessionNotCreatedException(
							"An error occurred while resigning the app '"
									+ file.getName() + "'. ", e1);
				}

				String appId = null;
				try {
					appId = app.getAppId();
				} catch (GalsenException e) {
					log.info("Ignoring app because an error occurred reading the app details: "
							+ file.getAbsolutePath());

					log.info(e.getMessage());
				}
				if ((appId != null) && (!(this.appsStore.containsKey(appId)))) {
					this.appsStore.put(appId, app);
					log.info("App "
							+ appId
							+ " has been added to galsen standalone server.");
				}
			} else {
				log.severe("Ignoring app because it was not found: "
						+ file.getAbsolutePath());
			}
		}

		if (!(serverConfiguration.isNoWebViewApp())) {
			try {
				AndroidApp app = this.galsenApkBuilder
						.resignApp(this.androidDriverAPKBuilder.extractAndroidDriverAPK());
				this.appsStore.put("android", app);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else {
			if (!(this.appsStore.isEmpty())) {
				return;
			}
			throw new GalsenException(
					"Fatal error initializing GalsenDriver: configured app(s) have not been found.");
		}
	}

	void initAndroidDevices() throws AndroidDeviceException {
		this.deviceManager = new DefaultDeviceManager(AndroidSdk.adb()
				.getAbsolutePath(),
				this.serverConfiguration.shouldKeepAdbAlive());
		this.deviceStore = new DeviceStore(
				Integer.valueOf(this.serverConfiguration.getEmulatorPort()),
				this.deviceManager);
		this.deviceStore.initAndroidDevices(new DefaultHardwareDeviceListener(
				this.deviceStore, this), this.serverConfiguration
				.shouldKeepAdbAlive());
	}

	@Override
	public String getServerVersion() {
		return GalsenServerBuilder.getJarVersionNumber();
	}

	@Override
	public String getCpuArch() {
		String arch = System.getProperty("os.arch");
		return arch;
	}

	@Override
	public String getOsName() {
		String os = System.getProperty("os.name");
		return os;
	}

	@Override
	public String getOsVersion() {
		String os = System.getProperty("os.version");
		return os;
	}

	protected GalsenConfiguration getGalsenConfiguration() {
		return this.serverConfiguration;
	}

	public String createNewTestSession(JSONObject caps, Integer retries)
			throws AndroidSdkException, JSONException {
		RemoteWebDriver driver;
		boolean appInstalledOnDevice;
		GalsenCapabilities desiredCapabilities = null;
		try {
			desiredCapabilities = new GalsenCapabilities(caps);
		} catch (JSONException e) {
			throw new GalsenException("Desired capabilities cannot be parsed.");
		}
		AndroidApp app = this.appsStore.get(desiredCapabilities.getAut());
		if (app == null) {
			throw new SessionNotCreatedException(
					"The requested application under test is not configured in galsen server.");
		}
		app = this.augmentApp(app, desiredCapabilities);
		AndroidDevice device = null;
		try {
			device = this.getAndroidDevice(desiredCapabilities);
		} catch (AndroidDeviceException e) {
			SessionNotCreatedException error = new SessionNotCreatedException(
					"Error occured while finding android device: "
							+ e.getMessage());
			e.printStackTrace();
			log.severe(error.getMessage());
			throw error;
		}
		if (device instanceof AndroidEmulator) {
			AndroidEmulator emulator = (AndroidEmulator) device;
			try {
				if (emulator.isEmulatorStarted()) {
					emulator.unlockEmulatorScreen();
				} else {
					HashMap<String, Object> config = new HashMap<String, Object>();
					if (this.serverConfiguration.getEmulatorOptions() != null) {
						config.put("OPTIONS",
								this.serverConfiguration.getEmulatorOptions());
					}
					config.put("TIMEOUT",
							this.serverConfiguration.getTimeoutEmulatorStart());
					if (desiredCapabilities.asMap().containsKey("display")) {
						Object d = desiredCapabilities.getCapability("display");
						config.put("DISPLAY", String.valueOf(d));
					}
					Locale locale = this.parseLocale(desiredCapabilities);
					emulator.start(locale, this.deviceStore.nextEmulatorPort()
							.intValue(), config);
				}
			} catch (AndroidDeviceException e) {
				this.deviceStore.release(device, app);
				if (retries > 0) {
					return this.createNewTestSession(caps, retries - 1);
				}
				throw new SessionNotCreatedException(
						"Error occured while interacting with the emulator: "
								+ (Object) emulator + ": " + e.getMessage());
			}
			emulator.setIDevice(this.deviceManager.getVirtualDevice(emulator
					.getAvdName()));
		}
		if (!(appInstalledOnDevice = device.isInstalled(app))
				|| this.serverConfiguration.isForceReinstall()) {
			device.install(app);
		} else {
			log.info("the app under test is already installed.");
		}
		int port = this.getNextGalsenServerPort();
		Boolean galsenInstalledSuccessfully = device
				.isInstalled("com.softhinkers.galsen." + app.getBasePackage());
		if (!galsenInstalledSuccessfully.booleanValue()
				|| this.serverConfiguration.isForceReinstall()) {
			AndroidApp galsenServer = this.createGalsenServerApk(app);
			galsenInstalledSuccessfully = device.install(galsenServer);
			if (!galsenInstalledSuccessfully.booleanValue()
					&& !device.install(galsenServer).booleanValue()) {
				this.deviceStore.release(device, app);
				if (retries > 0) {
					return this.createNewTestSession(caps, retries - 1);
				}
			}
		} else {
			log.info("galsen-server will not be created and installed because it already exists for the app under test.");
		}
		ArrayList<String> adbCommands = new ArrayList<String>();
		adbCommands.add("shell setprop log.tag.GALSEN "
				+ this.serverConfiguration.getLogLevel().name());
		adbCommands.addAll(desiredCapabilities.getPreSessionAdbCommands());
		for (String adbCommandParameter : adbCommands) {
			device.runAdbCommand(adbCommandParameter);
		}
		try {
			device.startGalsen(app, port);
		} catch (AndroidSdkException e) {
			log.info("error while starting galsen: " + e.getMessage());
			this.deviceStore.release(device, app);
			if (retries > 0) {
				return this.createNewTestSession(caps, retries - 1);
			}
			throw new SessionNotCreatedException(
					"Error occurred while starting instrumentation: "
							+ e.getMessage());
		}
		long start = System.currentTimeMillis();
		long startTimeOut = 20000;
		long timemoutEnd = start + startTimeOut;
		while (!device.isGalsenRunning()) {
			if (timemoutEnd >= System.currentTimeMillis()) {
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
				}
				continue;
			}
			throw new GalsenException(
					"Galsen server on the device didn't came up after "
							+ startTimeOut / 1000 + "sec:");
		}
		try {
			Thread.sleep(500);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		try {
			driver = new RemoteWebDriver(new URL("http://localhost:" + port
					+ "/wd/hub"), (Capabilities) desiredCapabilities);
		} catch (Exception e) {
			e.printStackTrace();
			this.deviceStore.release(device, app);
			throw new SessionNotCreatedException(
					"Error occurred while creating session on Android device",
					(Throwable) e);
		}
		String sessionId = driver.getSessionId().toString();
		GalsenCapabilities requiredCapabilities = new GalsenCapabilities(driver
				.getCapabilities().asMap());
		ActiveSession session = new ActiveSession(sessionId,
				requiredCapabilities, app, device, port, this);
		this.sessions.put(sessionId, session);
		if ("android".equals(desiredCapabilities.getAut())) {
			WebDriverWait wait = new WebDriverWait((WebDriver) driver, 60);
			wait.until((Function) ExpectedConditions
					.visibilityOfElementLocated((By) By
							.className((String) "android.webkit.WebView")));
			driver.switchTo().window("WEBVIEW");
			wait.until((Function) ExpectedConditions
					.visibilityOfElementLocated((By) By
							.id((String) "AndroidDriver")));
		}
		return sessionId;
	}

	private AndroidApp augmentApp(AndroidApp app,
			GalsenCapabilities desiredCapabilities) {
		AndroidApp returnApp = app;
		if (desiredCapabilities.getLaunchActivity() != null) {
			MultiActivityAndroidApp augmentedApp = new MultiActivityAndroidApp(
					(DefaultAndroidApp) returnApp);
			augmentedApp.setMainActivity(desiredCapabilities
					.getLaunchActivity());
			returnApp = augmentedApp;
		}
		return returnApp;
	}

	private AndroidApp createGalsenServerApk(AndroidApp aut)
			throws AndroidSdkException {
		if (!this.galsenServers.containsKey(aut.getAppId())) {
			try {
				AndroidApp galsenServer = this.galsenApkBuilder
						.createGalsenServer(aut);
				this.galsenServers.put(aut.getAppId(), galsenServer);
			} catch (Exception e) {
				e.printStackTrace();
				throw new SessionNotCreatedException(
						"An error occurred while building the galsen-server.apk for aut '"
								+ (Object) aut + "': " + e.getMessage());
			}
		}
		return this.galsenServers.get(aut.getAppId());
	}

	private Locale parseLocale(GalsenCapabilities capa) {
		if (capa.getLocale() == null) {
			return null;
		}
		String[] localeStr = capa.getLocale().split("_");
		Locale locale = new Locale(localeStr[0], localeStr[1]);
		return locale;
	}

	AndroidDevice getAndroidDevice(GalsenCapabilities caps)
			throws AndroidDeviceException {
		AndroidDevice device = null;
		try {
			device = this.deviceStore.findAndroidDevice(caps);
		} catch (DeviceStoreException e) {
			e.printStackTrace();
			log.fine(caps.getRawCapabilities().toString());
			throw new AndroidDeviceException(
					"Error occurred while looking for devices/emulators.",
					(Throwable) e);
		}
		return device;
	}

	Map<String, AndroidApp> getConfiguredApps() {
		return Collections.unmodifiableMap(this.appsStore);
	}

	void setDeviceStore(DeviceStore store) {
		this.deviceStore = store;
	}

	private synchronized int getNextGalsenServerPort() {
		return galsenServerPort++;
	}

	public List<ActiveSession> getActiveSessions() {
		return Lists.newArrayList(this.sessions.values());
	}

	public boolean isValidSession(String sessionId) {
		if (sessionId != null && !sessionId.isEmpty()) {
			return this.sessions.containsKey(sessionId);
		}
		return false;
	}

	public void stopSession(String sessionId) throws AndroidDeviceException {
		if (this.isValidSession(sessionId)) {
			ActiveSession session = this.sessions.get(sessionId);
			session.stopSessionTimer();
			try {
				HttpClientUtil.executeRequest(
						(String) ("http://localhost:"
								+ session.getGalsenServerPort()
								+ "/wd/hub/session/" + sessionId),
						(HttpMethod) HttpMethod.DELETE);
			} catch (Exception e) {
				// empty catch block
			}
			this.deviceStore.release(session.getDevice(), session.getAut());
			this.sessions.remove(sessionId);
			session = null;
		}
	}

	public void quitGalsen() {
		final List<String> sessionsToQuit = (List<String>) Lists
				.newArrayList((Collection) this.sessions.keySet());
		if (sessionsToQuit != null && !sessionsToQuit.isEmpty()) {
			for (final String sessionId : sessionsToQuit) {
				try {
					this.stopSession(sessionId);
				} catch (AndroidDeviceException e) {
					GalsenStandaloneDriver.log
							.severe("Error occured while stopping session: "
									+ e.getMessage());
					e.printStackTrace();
				}
			}
		}
		this.deviceManager.shutdown();
	}

	public GalsenCapabilities getSessionCapabilities(String sessionId) {
		if (this.sessions.containsKey(sessionId)) {
			return this.sessions.get(sessionId).getDesiredCapabilities();
		}
		return null;
	}

	public ActiveSession getActiveSession(String sessionId) {
		if (sessionId != null && this.sessions.containsKey(sessionId)) {
			return this.sessions.get(sessionId);
		}
		return null;
	}

	@Override
	public synchronized JSONArray getSupportedApps() {
		JSONArray list = new JSONArray();
		for (AndroidApp app : this.appsStore.values()) {
			JSONObject appInfo = new JSONObject();
			try {
				appInfo.put("appId", (Object) app.getAppId());
				appInfo.put("basePackage", (Object) app.getBasePackage());
				appInfo.put("mainActivity", (Object) app.getMainActivity());
				list.put((Object) appInfo);
			} catch (Exception e) {
			}
		}
		return list;
	}

	@Override
	public synchronized JSONArray getSupportedDevices() {
		JSONArray list = new JSONArray();
		for (AndroidDevice device : this.deviceStore.getDevices()) {
			JSONObject deviceInfo = new JSONObject();
			try {
				if (device instanceof DefaultAndroidEmulator) {
					deviceInfo.put("emulator", true);
					deviceInfo.put("avdName",
							(Object) ((DefaultAndroidEmulator) device)
									.getAvdName());
				} else {
					deviceInfo.put("emulator", false);
					deviceInfo.put("model",
							(Object) ((DefaultHardwareDevice) device)
									.getModel());
				}
				deviceInfo.put("platformVersion", (Object) device
						.getTargetPlatform().getApi());
				deviceInfo.put("screenSize", (Object) device.getScreenSize());
				list.put((Object) deviceInfo);
			} catch (Exception e) {
				log.info("Error occured when building suported device info: "
						+ e.getMessage());
			}
		}
		return list;
	}

	protected ActiveSession findActiveSession(AndroidDevice device) {
		for (ActiveSession session : this.sessions.values()) {
			if (!session.getDevice().equals((Object) device))
				continue;
			return session;
		}
		return null;
	}

	public byte[] takeScreenshot(String sessionId)
			throws AndroidDeviceException {
		if (sessionId == null || !this.sessions.containsKey(sessionId)) {
			throw new GalsenException("The given session id '" + sessionId
					+ "' was not found.");
		}
		return this.sessions.get(sessionId).getDevice().takeScreenshot();
	}

}
