package com.softhinkers.galsen;



import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.google.common.base.Throwables;
import com.softhinkers.galsen.exceptions.AndroidSdkException;
import com.softhinkers.galsen.log.LogLevelEnum;
import com.softhinkers.galsen.server.GalsenStandaloneServer;
import com.softhinkers.galsen.server.util.HttpClientUtil;

public class GalsenLauncher {
	public static final String LOGGER_NAME = "com.softhinkers.galsen";
	private static final Logger log = Logger.getLogger(GalsenLauncher.class
			.getName());
	private GalsenStandaloneServer server = null;
	private GalsenConfiguration config = null;

	public GalsenLauncher(GalsenConfiguration config) {
		this.config = config;
	}

	private void launchServer() {
		try {
			log.info("Starting galsen-server port " + this.config.getPort());
			this.server = new GalsenStandaloneServer(this.config);
			this.server.start();
		} catch (AndroidSdkException e) {
			log.severe("Galsen was not able to interact with the Android SDK: "
					+ e.getMessage());
			log.severe("Please make sure you have the latest version with the latest updates installed: ");

			log.severe("http://developer.android.com/sdk/index.html");
			throw Throwables.propagate(e);
		} catch (Exception e) {
			log.severe("Error occurred while building server: "
					+ e.getMessage());
			e.printStackTrace();
			throw Throwables.propagate(e);
		}
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				GalsenLauncher.log
						.info("Shutting down Galsen standalone");
				if (GalsenLauncher.this.server != null)
					GalsenLauncher.this.server.stop();
			}
		});
	}

	public void launchGalsen() {
		launchServer();
		HttpClientUtil.waitForServer(this.config.getPort());
	}

	public static void main(String[] args) {
		try {
			configureLogging();
		} catch (Exception e1) {
			log.severe("Error occurred while registering logging file handler.");
		}

		System.out.println("################# Galsen #################");
		GalsenConfiguration config = new GalsenConfiguration();
		try {
			new JCommander(config, args);
		} catch (ParameterException e) {
			log.severe("An error occurred while starting galsen: "
					+ e.getMessage());
			System.exit(0);
		}

		System.out
				.println("################# Configuration in use #################");
		System.out.println(config.toString());

		if (LogLevelEnum.ERROR.equals(config.getLogLevel()))
			Logger.getLogger("com.softhinkers.galsen").setLevel(
					LogLevelEnum.VERBOSE.level);
		else {
			Logger.getLogger("com.softhinkers.galsen").setLevel(
					config.getLogLevel().level);
		}

		GalsenLauncher laucher = new GalsenLauncher(config);
		laucher.launchServer();
	}

	private static void configureLogging() throws Exception {
		Handler fh = new FileHandler("%h/galsen.log", 2097152, 1);

		fh.setFormatter(new SimpleFormatter());
		Logger.getLogger("com.softhinkers.galsen").addHandler(fh);
	}

	public void stopGalsen() {
		if (this.server != null)
			this.server.stop();
	}
}