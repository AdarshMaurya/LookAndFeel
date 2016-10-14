package com.softhinkers.galsen.server;

import org.json.JSONException;
import org.json.JSONObject;

import com.softhinkers.galsen.server.http.HttpRequest;


public abstract class BaseRequestHandler {
	protected String mappedUri = null;

	public BaseRequestHandler(String mappedUri) {
		this.mappedUri = mappedUri;
	}

	public String getMappedUri() {
		return this.mappedUri;
	}

	public String getSessionId(HttpRequest request) {
		if (request.data().containsKey("SESSION_ID_KEY")) {
			return ((String) request.data().get("SESSION_ID_KEY"));
		}
		return null;
	}

	public String getCommandName(HttpRequest request) {
		if (request.data().containsKey("COMMAND_KEY")) {
			return ((String) request.data().get("COMMAND_KEY"));
		}
		return null;
	}

	public String getElementId(HttpRequest request) {
		if (request.data().containsKey("ELEMENT_ID_KEY")) {
			return ((String) request.data().get("ELEMENT_ID_KEY"));
		}
		return null;
	}

	public String getNameAttribute(HttpRequest request) {
		if (request.data().containsKey("NAME_ID_KEY")) {
			return ((String) request.data().get("NAME_ID_KEY"));
		}
		return null;
	}

	public JSONObject getPayload(HttpRequest request) throws JSONException {
		String json = request.body();
		if ((json != null) && (!(json.isEmpty()))) {
			return new JSONObject(json);
		}
		return new JSONObject();
	}

	public abstract Response handle(HttpRequest paramHttpRequest)
			throws JSONException;

	public boolean commandAllowedWithAlertPresentInWebViewMode() {
		return false;
	}
}