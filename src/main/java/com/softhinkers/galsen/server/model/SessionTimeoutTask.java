package com.softhinkers.galsen.server.model;

import java.util.TimerTask;
import java.util.logging.Logger;

import com.softhinkers.galsen.exceptions.AndroidDeviceException;

public class SessionTimeoutTask extends TimerTask {
	private static final Logger log = Logger.getLogger(SessionTimeoutTask.class
			.getName());
	private String sessionId;
	private GalsenStandaloneDriver driver;

	public SessionTimeoutTask(GalsenStandaloneDriver driver,
			String sessionId) {
		this.sessionId = sessionId;
		this.driver = driver;
	}

	public void run() {
		int sessionTimeout = this.driver.getGalsenConfiguration()
				.getSessionTimeoutMillis();
		log.info("Stopping session after configured session timeout of "
				+ (sessionTimeout / 1000) + " seconds.");
		try {
			this.driver.stopSession(this.sessionId);
		} catch (AndroidDeviceException e) {
			log.severe("While closing the session " + this.sessionId
					+ " after a session time out an error occurred: "
					+ e.getMessage());
		}
	}
}