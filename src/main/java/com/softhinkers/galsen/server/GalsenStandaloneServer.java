package com.softhinkers.galsen.server;

import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;

import com.softhinkers.galsen.GalsenConfiguration;
import com.softhinkers.galsen.exceptions.AndroidDeviceException;
import com.softhinkers.galsen.exceptions.AndroidSdkException;
import com.softhinkers.galsen.server.grid.SelfRegisteringRemote;
import com.softhinkers.galsen.server.http.HttpServer;
import com.softhinkers.galsen.server.model.GalsenStandaloneDriver;


public class GalsenStandaloneServer {

	private static final Logger log = Logger
			.getLogger(GalsenStandaloneServer.class.getName());
	private HttpServer webServer;
	private GalsenConfiguration configuration;
	private GalsenStandaloneDriver driver = null;

	protected GalsenStandaloneServer(GalsenConfiguration configuration,
			GalsenStandaloneDriver driver) throws AndroidSdkException {
		this.configuration = configuration;
		this.driver = driver;
		this.webServer = new HttpServer(configuration.getPort());
		init();
	}

	public GalsenStandaloneServer(GalsenConfiguration configuration)
			throws AndroidSdkException, AndroidDeviceException {
		this.configuration = configuration;
		this.webServer = new HttpServer(configuration.getPort());
		this.driver = initializeGalsenServer();
		init();
	}

	protected void init() throws AndroidSdkException {
		this.webServer.addHandler(new StatusServlet(this.driver));
		this.webServer.addHandler(new GalsenServlet(this.driver,
				this.configuration));
	}

	protected GalsenStandaloneDriver initializeGalsenServer()
			throws AndroidSdkException, AndroidDeviceException {
		return new GalsenStandaloneDriver(this.configuration);
	}

	public void start() {
		this.webServer.start();
		if ((!(StringUtils.isBlank(this.configuration.getRegistrationUrl())))
				&& (!(StringUtils.isBlank(this.configuration.getServerHost())))) {
			try {
				new SelfRegisteringRemote(this.configuration, this.driver)
						.performRegistration();
			} catch (Exception e) {
				log.severe("An error occured while registering galsen into grid hub.");
				e.printStackTrace();
			}
		}
		System.out
				.println("galsen-standalone server has been started on port: "
						+ this.configuration.getPort());
	}

	public void stop() {
		log.info("About to stop galsen-standalone server");
		this.driver.quitGalsen();
		this.webServer.stop();
	}

	public int getPort() {
		return this.webServer.getPort();
	}

	protected GalsenStandaloneDriver getDriver() {
		return this.driver;
	}
}