package com.softhinkers.galsen.server;



import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.softhinkers.galsen.server.http.HttpRequest;
import com.softhinkers.galsen.server.http.HttpResponse;
import com.softhinkers.galsen.server.http.HttpServlet;

public class StatusServlet implements HttpServlet {
	private ServerDetails galsenServer;
	private JSONArray apps = null;

	public StatusServlet(ServerDetails galsenServer) {
		this.galsenServer = galsenServer;
	}

	public void handleHttpRequest(HttpRequest httpRequest,
			HttpResponse httpResponse) throws Exception {
		if (!("/wd/hub/status".equals(httpRequest.uri()))) {
			return;
		}
		if (!("GET".equalsIgnoreCase(httpRequest.method()))) {
			httpResponse.setStatus(404).end();
			return;
		}

		JSONObject result = createDetailedStatusResponse();

		httpResponse.setContentType("application/json").setStatus(200)
				.setContent(result.toString()).end();
	}

	private JSONObject createDetailedStatusResponse() throws JSONException {
		JSONObject build = new JSONObject();
		build.put("version", this.galsenServer.getServerVersion());
		build.put("browserName", "galsen");

		JSONObject os = new JSONObject();
		os.put("arch", this.galsenServer.getCpuArch());
		os.put("name", this.galsenServer.getOsName());
		os.put("version", this.galsenServer.getOsVersion());

		JSONObject json = new JSONObject();
		json.put("build", build);
		json.put("os", os);

		JSONArray devices = null;
		try {
			devices = this.galsenServer.getSupportedDevices();
		} catch (Exception e) {
			devices = new JSONArray();
		}

		json.put("supportedDevices", devices);

		if ((this.apps == null) || (devices.length() == 0)) {
			try {
				this.apps = this.galsenServer.getSupportedApps();
			} catch (Exception e) {
				this.apps = new JSONArray();
			}
		}
		json.put("supportedApps", this.apps);
		JSONObject result = new JSONObject();
		result.put("status", 0);
		result.put("value", json);
		return result;
	}
}