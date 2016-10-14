package com.softhinkers.galsen;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.softhinkers.galsen.log.LogLevelConverter;
import com.softhinkers.galsen.log.LogLevelEnum;


public class GalsenConfiguration {
	@Parameter(description = "port the server will listen on.", names = { "-port" })
	private int port;

	@Parameter(description = "timeout that will be used to start Android emulators", names = { "-timeoutEmulatorStart" })
	private long timeoutEmulatorStart;

	@Parameter(description = "location of the application under test. Absolute path to the apk", names = {
			"-app", "-aut" })
	private List<String> supportedApps;

	@Deprecated
	@Parameter(names = { "-verbose" }, description = "Debug mode")
	private boolean verbose;

	@Parameter(names = { "-emulatorPort" }, description = "port number to start running emulators on")
	private int emulatorPort;

	@Parameter(names = { "-deviceScreenshot" }, description = "if true, screenshots will be taken on the device instead of using the ddmlib libary.")
	private boolean deviceScreenshot;

	@Parameter(description = "the port the galsen-standalone is using to communicate with instrumentation server", names = { "-galsenServerPort" })
	private int galsenServerPort;

	@Parameter(description = "The file of the keystore to be used", names = { "-keystore" })
	private String keystore;

	@Parameter(description = "The emulator options used for starting emulators: e.g. -no-audio", names = { "-emulatorOptions" })
	private String emulatorOptions;

	@Parameter(description = "if specified, will send a registration request to the given url. Example : http://localhost:4444/grid/register", names = { "-hub" })
	private String registrationUrl;

	@Parameter(description = "if specified, will specify the remote proxy to use on the grid. Example : com.softhinkers.galsen.grid.GalSenSessionProxy", names = { "-proxy" })
	private String proxy;

	@Parameter(description = "host of the node. Ip address needs to be specified for registering to a grid hub (guessing can be wrong complex).", names = { "-host" })
	private String serverHost;

	@Parameter(names = { "-keepAdbAlive" }, description = "If true, adb will not be terminated on server shutdown.")
	private boolean keepAdbAlive;

	@Parameter(names = { "-noWebviewApp" }, description = "If you don't want galsen to auto-extract and have 'AndroidDriver' (webview only app) available.")
	private boolean noWebViewApp;

	@Parameter(names = { "-noClearData" }, description = "When you quit the app, shell pm clear will not be called with this option specified.")
	private boolean noClearData;

	@Parameter(description = "maximum session duration in seconds. Session will be forcefully terminated if it takes longer.", names = { "-sessionTimeout" })
	private int sessionTimeoutSeconds;

	@Parameter(names = { "-forceReinstall" }, description = "Forces Galsen Server and the app under test to be reinstalled (for Galsen developers)")
	private boolean forceReinstall;

	@Parameter(names = { "-logLevel" }, converter = LogLevelConverter.class, description = "Specifies the log level of Galsen. Available values are: ERROR, WARNING, INFO, DEBUG and VERBOSE.")
	private LogLevelEnum logLevel;

	public static GalsenConfiguration create(String[] args) {
		GalsenConfiguration res = new GalsenConfiguration();
	    new JCommander(res).parse(args);
	    return res;
	  }
	
	public GalsenConfiguration() {
		this.port = 4444;
		this.timeoutEmulatorStart = 300000L;
		this.supportedApps = new ArrayList();
		this.verbose = false;
		this.emulatorPort = 5560;
		this.deviceScreenshot = false;
		this.galsenServerPort = 8080;
		this.keystore = null;
		this.emulatorOptions = null;
		this.registrationUrl = null;
		this.proxy = null;
		this.keystore = null;
		this.emulatorOptions = null;
		this.registrationUrl = null;
		this.proxy = null;
		this.keepAdbAlive = false;
		this.noWebViewApp = false;
		this.noClearData = false;
		this.sessionTimeoutSeconds = 1800;
		this.forceReinstall = false;
		this.logLevel = LogLevelEnum.ERROR;
	}

	public String getKeystore() {
		return this.keystore;
	}

	public void setGalsenServerPort(int galsenServerPort) {
		this.galsenServerPort = galsenServerPort;
	}

	public int getGalsenServerPort() {
		return this.galsenServerPort;
	}

	public void addSupportedApp(String appAbsolutPath) {
		this.supportedApps.add(appAbsolutPath);
	}

	public List<String> getSupportedApps() {
		return this.supportedApps;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getPort() {
		return this.port;
	}

	public void setEmulatorPort(int port) {
		this.emulatorPort = port;
	}

	public int getEmulatorPort() {
		return this.emulatorPort;
	}

	public long getTimeoutEmulatorStart() {
		return this.timeoutEmulatorStart;
	}

	public void setTimeoutEmulatorStart(long timeoutEmulatorStart) {
		this.timeoutEmulatorStart = timeoutEmulatorStart;
	}

	public boolean isDeviceScreenshot() {
		return this.deviceScreenshot;
	}

	public void setDeviceScreenshot(boolean deviceScreenshot) {
		this.deviceScreenshot = deviceScreenshot;
	}

	public String getRegistrationUrl() {
		return this.registrationUrl;
	}

	public void setRegistrationUrl(String registrationUrl) {
		this.registrationUrl = registrationUrl;
	}

	public String getProxy() {
		return this.proxy;
	}

	public void setProxy(String proxy) {
		this.proxy = proxy;
	}

	public String getServerHost() {
		return this.serverHost;
	}

	public void setServerHost(String serverHost) {
		this.serverHost = serverHost;
	}

	public String getEmulatorOptions() {
		return this.emulatorOptions;
	}

	public void setEmulatorOptions(String qemu) {
		this.emulatorOptions = qemu;
	}

	public boolean shouldKeepAdbAlive() {
		return this.keepAdbAlive;
	}

	public void setShouldKeepAdbAlive(boolean keepAdbAlive) {
		this.keepAdbAlive = keepAdbAlive;
	}

	public boolean isNoWebViewApp() {
		return this.noWebViewApp;
	}

	public void setNoWebViewApp(boolean noWebViewApp) {
		this.noWebViewApp = noWebViewApp;
	}

	public boolean isNoClearData() {
		return this.noClearData;
	}

	public void setNoClearData(boolean noClearData) {
		this.noClearData = noClearData;
	}

	public int getSessionTimeoutMillis() {
		return (this.sessionTimeoutSeconds * 1000);
	}

	public void setSessionTimeoutSeconds(int sessionTimeoutSeconds) {
		this.sessionTimeoutSeconds = sessionTimeoutSeconds;
	}

	public boolean isForceReinstall() {
		return this.forceReinstall;
	}

	public void setForceReinstall(boolean forceReinstall) {
		this.forceReinstall = forceReinstall;
	}

	public LogLevelEnum getLogLevel() {
		return this.logLevel;
	}

	public void setLogLevel(LogLevelEnum logLevel) {
		this.logLevel = logLevel;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this,
				ToStringStyle.MULTI_LINE_STYLE);
	}

}
