package com.softhinkers.galsen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.softhinkers.galsen.device.DeviceTargetPlatform;

public class GalsenCapabilities extends DesiredCapabilities {
	private static final long serialVersionUID = -7061568919298342363L;
	private static final String GALSEN = "galsen";
	public static final String AUT = "aut";
	public static final String EMULATOR = "emulator";
	public static final String DISPLAY = "display";
	public static final String LOCALE = "locale";
	public static final String SCREEN_SIZE = "screenSize";
	public static final String PRE_SESSION_ADB_COMMANDS = "preSessionAdbCommands";
	public static final String SERIAL = "serial";
	public static final String PLATFORM_VERSION = "platformVersion";
	public static final String PLATFORM_NAME = "platformName";
	public static final String AUTOMATION_NAME = "automationName";
	public static final String LAUNCH_ACTIVITY = "launchActivity";

	public GalsenCapabilities(Map<String, ?> from) {
		for (String key : from.keySet()) {
			this.setCapability(key, from.get(key));
		}
	}

	public String getSerial() {
		if (this.getRawCapabilities().get("serial") == null
				|| this.getRawCapabilities().get("serial")
						.equals(JSONObject.NULL)) {
			return null;
		}
		return (String) this.getRawCapabilities().get("serial");
	}

	public String getPlatformVersion() {
		return (String) this.getRawCapabilities().get("platformVersion");
	}

	public String getAut() {
		return (String) this.getRawCapabilities().get("aut");
	}

	public String getLaunchActivity() {
		return (String) this.getRawCapabilities().get("launchActivity");
	}

	public Boolean getEmulator() {
		if (this.getRawCapabilities().get("emulator") == null
				|| this.getRawCapabilities().get("emulator")
						.equals(JSONObject.NULL)) {
			return null;
		}
		return (Boolean) this.getRawCapabilities().get("emulator");
	}

	public String getPlatformName() {
		return (String) this.getRawCapabilities().get("platformName");
	}

	public String getAutomationName() {
		return (String) this.getRawCapabilities().get("automationName");
	}

	public String getLocale() {
		return (String) this.getRawCapabilities().get("locale");
	}

	public Map<String, Object> getRawCapabilities() {
		return (Map<String, Object>) this.asMap();
	}

	public String getScreenSize() {
		return (String) this.getRawCapabilities().get("screenSize");
	}

	public void setSerial(String serial) {
		this.setCapability("serial", serial);
	}

	public void setPlatformVersion(DeviceTargetPlatform androidTarget) {
		this.setCapability("platformVersion", androidTarget.getApi());
	}

	public void setAut(String aut) {
		this.setCapability("aut", aut);
	}

	public void setLaunchActivity(String launchActivity) {
		this.setCapability("launchActivity", launchActivity);
	}

	public void setEmulator(Boolean emulator) {
		this.setCapability("emulator", (Object) emulator);
	}

	public void setLocale(String locale) {
		this.setCapability("locale", locale);
	}

	public void setScreenSize(String screenSize) {
		this.setCapability("screenSize", screenSize);
	}

	public GalsenCapabilities(JSONObject source) throws JSONException {
		Iterator iter = source.keys();
		while (iter.hasNext()) {
			String key = (String) iter.next();
			Object value = source.get(key);
			this.setCapability(key, this.decode(value));
		}
		if (source.has("browserName") && !source.has("aut")) {
			this.setAut(source.getString("browserName"));
		}
	}

	public GalsenCapabilities() {
		this.setCapability("automationName", "selendroid");
		this.setBrowserName("selendroid");
		this.setCapability("platformName", "android");
	}

	public GalsenCapabilities(String aut) {
		this.setAut(aut);
	}

	public GalsenCapabilities(String serial, String aut) {
		this.setAut(aut);
		this.setSerial(serial);
		if (serial == null) {
			this.setEmulator(null);
		} else if (serial.startsWith("emulator")) {
			this.setEmulator(true);
		} else {
			this.setEmulator(false);
		}
	}

	public static GalsenCapabilities emulator(String aut) {
		GalsenCapabilities caps = new GalsenCapabilities();
		caps.setAut(aut);
		caps.setEmulator(true);
		return caps;
	}

	public static GalsenCapabilities emulator(
			DeviceTargetPlatform platform, String aut) {
		GalsenCapabilities caps = new GalsenCapabilities();
		caps.setPlatformVersion(platform);
		caps.setAut(aut);
		caps.setEmulator(true);
		return caps;
	}

	public static DesiredCapabilities android(DeviceTargetPlatform platform) {
		GalsenCapabilities capabilities = new GalsenCapabilities();
		capabilities.setCapability("browserName", "android");
		capabilities.setCapability("version", "");
		capabilities.setCapability("platform", "android");
		capabilities.setCapability("platformName", "android");
		capabilities.setCapability("platformVersion", platform.getApi());
		return capabilities;
	}

	public List<String> getPreSessionAdbCommands() {
		ArrayList<String> res = new ArrayList<String>();
		Object capa = this.getCapability("preSessionAdbCommands");
		if (capa != null) {
			res.addAll((Collection) capa);
		}
		return res;
	}

	public void setPreSessionAdbCommands(List<String> commands) {
		this.setCapability("preSessionAdbCommands", commands);
	}

	public static GalsenCapabilities device(DeviceTargetPlatform platform,
			String aut) {
		GalsenCapabilities caps = GalsenCapabilities.emulator(platform,
				aut);
		caps.setEmulator(false);
		return caps;
	}

	public static GalsenCapabilities device(String aut) {
		GalsenCapabilities caps = GalsenCapabilities.emulator(aut);
		caps.setEmulator(false);
		return caps;
	}

	private Object decode(Object o) throws JSONException {
		if (o instanceof JSONArray) {
			ArrayList<Object> res = new ArrayList<Object>();
			JSONArray array = (JSONArray) o;
			for (int i = 0; i < array.length(); ++i) {
				Object r = array.get(i);
				res.add(this.decode(r));
			}
			return res;
		}
		return o;
	}
}