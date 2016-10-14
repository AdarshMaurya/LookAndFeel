package com.softhinkers.galsen.server.handler;

import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import com.softhinkers.galsen.server.BaseGalsenServerHandler;
import com.softhinkers.galsen.server.GalsenResponse;
import com.softhinkers.galsen.server.Response;
import com.softhinkers.galsen.server.http.HttpRequest;
import com.softhinkers.galsen.server.model.ActiveSession;

public class AdbSendText extends BaseGalsenServerHandler {
	private static final Logger log = Logger.getLogger(AdbSendText.class
			.getName());

	public AdbSendText(String mappedUri) {
		super(mappedUri);
	}

	public Response handle(HttpRequest request) throws JSONException {
		JSONObject payload = getPayload(request);
		log.info("Send text via adb: " + payload.toString(2));
		ActiveSession session = getGalsenDriver(request).getActiveSession(
				getSessionId(request));

		session.getDevice().runAdbCommand(
				"shell input text '" + payload.getString("text") + "'");
		return new GalsenResponse(getSessionId(request), "");
	}
}