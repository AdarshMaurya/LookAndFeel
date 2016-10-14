package com.softhinkers.galsen.server.grid;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHttpEntityEnclosingRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.softhinkers.galsen.GalsenConfiguration;
import com.softhinkers.galsen.exceptions.GalsenException;
import com.softhinkers.galsen.server.model.GalsenStandaloneDriver;
import com.softhinkers.galsen.server.util.HttpClientUtil;

public class SelfRegisteringRemote {
	private static final Logger log = Logger
			.getLogger(SelfRegisteringRemote.class.getName());
	private GalsenConfiguration config;
	private GalsenStandaloneDriver driver;

	public SelfRegisteringRemote(GalsenConfiguration config,
			GalsenStandaloneDriver driver) {
		this.config = config;
		this.driver = driver;
	}

	public void performRegistration() throws Exception {
		String tmp = this.config.getRegistrationUrl();

		HttpClient client = HttpClientUtil.getHttpClient();

		URL registration = new URL(tmp);
		if (log.isLoggable(Level.INFO)) {
			log.info("Registering galsen node to Galsen Grid hub :"
					+ registration);
		}
		BasicHttpEntityEnclosingRequest r = new BasicHttpEntityEnclosingRequest(
				"POST", registration.toExternalForm());

		JSONObject nodeConfig = getNodeConfig();
		r.setEntity(new StringEntity(nodeConfig.toString()));

		HttpHost host = new HttpHost(registration.getHost(),
				registration.getPort());
		HttpResponse response = client.execute(host, r);
		if (response.getStatusLine().getStatusCode() != 200)
			throw new GalsenException(
					"Error sending the registration request.");
	}

	private JSONObject getNodeConfig() {
		JSONObject res = new JSONObject();
		try {
			res.put("class", "org.openqa.grid.common.RegistrationRequest");
			res.put("configuration", getConfiguration());
			JSONArray caps = new JSONArray();
			JSONArray devices = this.driver.getSupportedDevices();
			for (int i = 0; i < devices.length(); ++i) {
				JSONObject device = (JSONObject) devices.get(i);
				JSONObject capa = new JSONObject();
				capa.put("screenSize", device.getString("screenSize"));

				String version = device.getString("platformVersion");
				capa.put("platformVersion", version);
				capa.put("emulator", device.getString("emulator"));
				capa.put("browserName", "selendroid");
				capa.put("platform", "ANDROID");
				capa.put("version", version);
				caps.put(capa);
			}
			res.put("capabilities", caps);
		} catch (JSONException e) {
			throw new GalsenException(e.getMessage(), e);
		}

		return res;
	}

	private JSONObject getConfiguration() throws JSONException {
		JSONObject configuration = new JSONObject();

		configuration.put("port", this.config.getPort());
		configuration.put("register", true);

		if (this.config.getProxy() != null)
			configuration.put("proxy", this.config.getProxy());
		else {
			configuration.put("proxy",
					"org.openqa.grid.selenium.proxy.DefaultRemoteProxy");
		}

		configuration.put("role", "node");
		configuration.put("registerCycle", 5000);
		configuration.put("maxInstances", 5);
		URL registrationUrl;
		try {
			registrationUrl = new URL(this.config.getRegistrationUrl());
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new GalsenException("Grid hub url cannot be parsed: "
					+ e.getMessage());
		}
		configuration.put("hubHost", registrationUrl.getHost());
		configuration.put("hubPort", registrationUrl.getPort());

		configuration.put("remoteHost", "http://" + this.config.getServerHost()
				+ ":" + this.config.getPort());
		return configuration;
	}
}