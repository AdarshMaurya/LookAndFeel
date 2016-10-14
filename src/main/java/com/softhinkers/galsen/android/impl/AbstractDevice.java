package com.softhinkers.galsen.android.impl;


import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteResultHandler;
import org.apache.commons.exec.ExecuteStreamHandler;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.RawImage;
import com.android.ddmlib.TimeoutException;
import com.beust.jcommander.internal.Lists;
import com.google.common.collect.ObjectArrays;
import com.softhinkers.galsen.android.AndroidApp;
import com.softhinkers.galsen.android.AndroidDevice;
import com.softhinkers.galsen.android.AndroidSdk;
import com.softhinkers.galsen.exceptions.AndroidDeviceException;
import com.softhinkers.galsen.exceptions.AndroidSdkException;
import com.softhinkers.galsen.exceptions.GalsenException;
import com.softhinkers.galsen.exceptions.ShellCommandException;
import com.softhinkers.galsen.io.ShellCommand;
import com.softhinkers.galsen.log.LogEntry;

public abstract class AbstractDevice implements AndroidDevice {
	private static final Logger log = Logger.getLogger(AbstractDevice.class
			.getName());
	public static final String WD_STATUS_ENDPOINT = "http://localhost:8080/wd/hub/status";
	protected String serial = null;
	protected Integer port = null;
	protected IDevice device;
	private ByteArrayOutputStream logoutput;
	private ExecuteWatchdog logcatWatchdog;
	private static final Integer COMMAND_TIMEOUT = 20000;

	public AbstractDevice(String serial) {
		this.serial = serial;
	}

	public AbstractDevice(IDevice device) {
		this.device = device;
		this.serial = device.getSerialNumber();
	}

	protected AbstractDevice() {
	}

	protected boolean isSerialConfigured() {
		return this.serial != null && !this.serial.isEmpty();
	}

	public void setVerbose() {
		log.setLevel(Level.FINEST);
	}

	public boolean isDeviceReady() {
		CommandLine command = this.adbCommand("shell",
				"getprop init.svc.bootanim");
		String bootAnimDisplayed = null;
		try {
			bootAnimDisplayed = ShellCommand.exec((CommandLine) command);
		} catch (ShellCommandException e) {
			// empty catch block
		}
		if (bootAnimDisplayed != null && bootAnimDisplayed.contains("stopped")) {
			return true;
		}
		return false;
	}

	public boolean isInstalled(String appBasePackage)
			throws AndroidSdkException {
		CommandLine command = this
				.adbCommand("shell", "pm", "list", "packages");
		command.addArgument(appBasePackage, false);
		String result = null;
		try {
			result = ShellCommand.exec((CommandLine) command);
		} catch (ShellCommandException e) {
			// empty catch block
		}
		if (result != null && result.contains("package:" + appBasePackage)) {
			return true;
		}
		return false;
	}

	public boolean isInstalled(AndroidApp app) throws AndroidSdkException {
		return this.isInstalled(app.getBasePackage());
	}

	public Boolean install(AndroidApp app) {
		CommandLine command = this.adbCommand("install", "-r",
				app.getAbsolutePath());
		String out = this.executeCommand(command, COMMAND_TIMEOUT * 6);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ie) {
			throw new RuntimeException(ie);
		}
		return out.contains("Success");
	}

	public boolean start(AndroidApp app) throws AndroidSdkException {
		if (!this.isInstalled(app)) {
			this.install(app);
		}
		String mainActivity = app.getMainActivity().replace(
				app.getBasePackage(), "");
		CommandLine command = this.adbCommand("shell", "am", "start", "-a",
				"android.intent.action.MAIN", "-n", app.getBasePackage() + "/"
						+ mainActivity);
		String out = this.executeCommand(command);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ie) {
			throw new RuntimeException(ie);
		}
		return out.contains("Starting: Intent");
	}

	protected String executeCommand(CommandLine command) {
		return this.executeCommand(command, COMMAND_TIMEOUT.intValue());
	}

	protected String executeCommand(CommandLine command, long timeout) {
		try {
			return ShellCommand.exec((CommandLine) command, (long) timeout);
		} catch (ShellCommandException e) {
			e.printStackTrace();
			return "";
		}
	}

	public void uninstall(AndroidApp app) throws AndroidSdkException {
		CommandLine command = this
				.adbCommand("uninstall", app.getBasePackage());
		this.executeCommand(command);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ie) {
			throw new RuntimeException(ie);
		}
	}

	public void clearUserData(AndroidApp app) throws AndroidSdkException {
		CommandLine command = this.adbCommand("shell", "pm", "clear",
				app.getBasePackage());
		this.executeCommand(command);
	}

	public void kill(AndroidApp aut) throws AndroidDeviceException,
			AndroidSdkException {
		CommandLine command = this.adbCommand("shell", "am", "force-stop",
				aut.getBasePackage());
		this.executeCommand(command);
		if (this.logcatWatchdog != null && this.logcatWatchdog.isWatching()) {
			this.logcatWatchdog.destroyProcess();
			this.logcatWatchdog = null;
		}
	}

	public void startGalsen(AndroidApp aut, int port)
			throws AndroidSdkException {
		this.port = port;
		Object[] args = new String[] {
				"-e",
				"main_activity",
				aut.getMainActivity(),
				"-e",
				"server_port",
				Integer.toString(port),
				"com.softhinkers.galsen." + aut.getBasePackage()
						+ "/io.com.softhinkers.galsen.ServerInstrumentation" };
		CommandLine command = this.adbCommand((String[]) ObjectArrays.concat(
				(Object[]) new String[] { "shell", "am", "instrument" },
				(Object[]) args, (Class) String.class));
		String result = this.executeCommand(command);
		if (result.contains("FAILED")) {
			String detailedResult;
			try {
				CommandLine getErrorDetailCommand = this
						.adbCommand((String[]) ObjectArrays.concat(
								(Object[]) new String[] { "shell", "am",
										"instrument", "-w" }, (Object[]) args,
								(Class) String.class));
				detailedResult = this.executeCommand(getErrorDetailCommand);
			} catch (Exception e) {
				detailedResult = "";
			}
			throw new GalsenException(
					"Error occurred while starting galsen-server on the device",
					new Throwable(result + "\nDetails:\n" + detailedResult));
		}
		this.forwardGalsenPort(port);
		this.startLogging();
	}

	public void forwardPort(int local, int remote) {
		CommandLine command = this.adbCommand("forward", "tcp:" + local, "tcp:"
				+ remote);
		this.executeCommand(command);
	}

	private void forwardGalsenPort(int port) {
		this.forwardPort(port, port);
	}

	public boolean isGalsenRunning() {
		String responseValue;
		DefaultHttpClient httpClient = new DefaultHttpClient();
		String url = "http://localhost:8080/wd/hub/status".replace("8080",
				String.valueOf(this.port));
		log.info("using url: " + url);
		HttpGet request = new HttpGet(url);
		HttpResponse response = null;
		try {
			response = httpClient.execute((HttpUriRequest) request);
		} catch (Exception e) {
			log.severe("Error getting status: " + e);
			return false;
		}
		int statusCode = response.getStatusLine().getStatusCode();
		log.info("got response status code: " + statusCode);
		try {
			responseValue = IOUtils.toString((InputStream) response.getEntity()
					.getContent());
			log.info("got response value: " + responseValue);
		} catch (Exception e) {
			log.severe("Error getting status: " + e);
			return false;
		}
		if (response != null && 200 == statusCode
				&& responseValue.contains("galsen")) {
			return true;
		}
		return false;
	}

	public int getGalensPort() {
		return this.port;
	}

	public List<LogEntry> getLogs() {
		List logs = Lists.newArrayList();
		String result = this.logoutput != null ? this.logoutput.toString() : "";
		String[] lines = result.split("\\r?\\n");
		int num_lines = lines.length;
		log.fine("getting logcat");
		for (int x = 0; x < num_lines; ++x) {
			Level l = lines[x].startsWith("I") ? Level.INFO : (lines[x]
					.startsWith("W") ? Level.WARNING : (lines[x]
					.startsWith("S") ? Level.SEVERE : Level.FINE));
			logs.add(new LogEntry(l, System.currentTimeMillis(), lines[x]));
			log.fine(lines[x]);
		}
		return logs;
	}

	private void startLogging() {
		this.logoutput = new ByteArrayOutputStream();
		DefaultExecutor exec = new DefaultExecutor();
		exec.setStreamHandler((ExecuteStreamHandler) new PumpStreamHandler(
				(OutputStream) this.logoutput));
		CommandLine command = this.adbCommand("logcat", "ResourceType:S",
				"dalvikvm:S", "Trace:S", "SurfaceFlinger:S", "StrictMode:S",
				"ExchangeService:S", "SVGAndroid:S", "skia:S",
				"LoaderManager:S", "ActivityThread:S", "-v", "time");
		log.info("starting logcat:");
		log.fine(command.toString());
		try {
			exec.execute(command,
					(ExecuteResultHandler) new DefaultExecuteResultHandler());
			this.logcatWatchdog = new ExecuteWatchdog(-1);
			exec.setWatchdog(this.logcatWatchdog);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	protected String getProp(String key) {
		CommandLine command = this.adbCommand("shell", "getprop", key);
		String prop = this.executeCommand(command);
		return prop == null ? "" : prop.replace("\r", "").replace("\n", "");
	}

	protected static String extractValue(String regex, String output) {
		Pattern pattern = Pattern.compile(regex, 8);
		Matcher matcher = pattern.matcher(output);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return "";
	}

	public boolean screenSizeMatches(String requestedScreenSize) {
		if (requestedScreenSize == null || requestedScreenSize.isEmpty()) {
			return true;
		}
		return this.getScreenSize().equals(requestedScreenSize);
	}

	public void runAdbCommand(String parameter) {
		if (parameter == null || parameter.isEmpty()) {
			return;
		}
		System.out.println("running command: adb " + parameter);
		CommandLine command = this.adbCommand();
		String[] params = parameter.split(" ");
		for (int i = 0; i < params.length; ++i) {
			command.addArgument(params[i], false);
		}
		this.executeCommand(command);
	}

	public byte[] takeScreenshot() throws AndroidDeviceException {
		RawImage rawImage;
		if (this.device == null) {
			throw new AndroidDeviceException(
					"Device not accessible via ddmlib.");
		}
		try {
			rawImage = this.device.getScreenshot();
		} catch (IOException ioe) {
			throw new AndroidDeviceException("Unable to get frame buffer: "
					+ ioe.getMessage());
		} catch (TimeoutException e) {
			e.printStackTrace();
			throw new AndroidDeviceException(e.getMessage());
		} catch (AdbCommandRejectedException e) {
			e.printStackTrace();
			throw new AndroidDeviceException(e.getMessage());
		}
		if (rawImage == null) {
			return null;
		}
		BufferedImage image = new BufferedImage(rawImage.width,
				rawImage.height, 2);
		int index = 0;
		int IndexInc = rawImage.bpp >> 3;
		for (int y = 0; y < rawImage.height; ++y) {
			for (int x = 0; x < rawImage.width; ++x) {
				int value = rawImage.getARGB(index);
				index += IndexInc;
				image.setRGB(x, y, value);
			}
		}
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try {
			if (!ImageIO.write((RenderedImage) image, "png", stream)) {
				throw new IOException("Failed to find png writer");
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw new AndroidDeviceException(e.getMessage());
		}
		byte[] raw = null;
		try {
			stream.flush();
			raw = stream.toByteArray();
			stream.close();
		} catch (IOException e) {
			throw new RuntimeException("I/O Error while capturing screenshot: "
					+ e.getMessage());
		} finally {
			ByteArrayOutputStream closeable = stream;
			try {
				if (closeable != null) {
					closeable.close();
				}
			} catch (IOException ioe) {
			}
		}
		return raw;
	}

	public void inputKeyevent(int value) {
		this.executeCommand(this.adbCommand("shell", "input", "keyevent", ""
				+ value));
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void invokeActivity(String activity) {
		this.executeCommand(this.adbCommand("shell", "am", "start", "-a",
				activity));
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void restartADB() {
		this.executeCommand(this.adbCommand("kill-server"));
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		this.executeCommand(this.adbCommand("devices"));
	}

	private CommandLine adbCommand() {
		CommandLine command = new CommandLine(AndroidSdk.adb());
		if (this.isSerialConfigured()) {
			command.addArgument("-s", false);
			command.addArgument(this.serial, false);
		}
		return command;
	}

	private CommandLine adbCommand(String... args) {
		CommandLine command = this.adbCommand();
		for (String arg : args) {
			command.addArgument(arg, false);
		}
		return command;
	}
}