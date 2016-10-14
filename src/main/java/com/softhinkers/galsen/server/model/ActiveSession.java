package com.softhinkers.galsen.server.model;



import java.util.Timer;

import com.softhinkers.galsen.GalsenCapabilities;
import com.softhinkers.galsen.android.AndroidApp;
import com.softhinkers.galsen.android.AndroidDevice;

public class ActiveSession {

	private final String sessionKey;
	private AndroidApp aut;
	private AndroidDevice device;
	private GalsenCapabilities desiredCapabilities;
	private final int galsenServerPort;
	private boolean invalid = false;
	private final Timer stopSessionTimer = new Timer(true);

	ActiveSession(String sessionKey,
			GalsenCapabilities desiredCapabilities, AndroidApp aut,
			AndroidDevice device, int selendroidPort,
			GalsenStandaloneDriver driver) {
		this.galsenServerPort = selendroidPort;
		this.sessionKey = sessionKey;
		this.aut = aut;
		this.device = device;
		this.desiredCapabilities = desiredCapabilities;
		this.stopSessionTimer.schedule(new SessionTimeoutTask(driver,
				sessionKey), driver.getGalsenConfiguration()
				.getSessionTimeoutMillis());
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (super.getClass() != obj.getClass())
			return false;
		ActiveSession other = (ActiveSession) obj;
		if (this.sessionKey == null)
			if (other.sessionKey != null)
				return false;
			else if (!(this.sessionKey.equals(other.sessionKey)))
				return false;
		return true;
	}

	public AndroidApp getAut() {
		return this.aut;
	}

	public int getGalsenServerPort() {
		return this.galsenServerPort;
	}

	public GalsenCapabilities getDesiredCapabilities() {
		return this.desiredCapabilities;
	}

	public AndroidDevice getDevice() {
		return this.device;
	}

	public String getSessionKey() {
		return this.sessionKey;
	}

	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = 31 * result
				+ ((this.sessionKey == null) ? 0 : this.sessionKey.hashCode());
		return result;
	}

	public boolean isInvalid() {
		return this.invalid;
	}

	public void invalidate() {
		this.invalid = true;
	}

	public void stopSessionTimer() {
		this.stopSessionTimer.cancel();
	}

	public String toString() {
		return "ActiveSession [sessionKey=" + this.sessionKey + ", aut="
				+ this.aut + ", device=" + this.device + "]";
	}
}