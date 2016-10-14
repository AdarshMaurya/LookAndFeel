package com.softhinkers.galsen.server.handler;



import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import com.softhinkers.galsen.server.BaseGalsenServerHandler;
import com.softhinkers.galsen.server.GalsenResponse;
import com.softhinkers.galsen.server.Response;
import com.softhinkers.galsen.server.http.HttpRequest;
import com.softhinkers.galsen.server.model.ActiveSession;

public class AdbSendKeyEvent extends BaseGalsenServerHandler {
	private static final Logger log = Logger.getLogger(AdbSendKeyEvent.class
			.getName());

	public AdbSendKeyEvent(String mappedUri) {
		super(mappedUri);
	}

	public Response handle(HttpRequest request) throws JSONException {
		JSONObject payload = getPayload(request);
		log.info("Send Key Event via adb: " + payload.toString(2));
		ActiveSession session = getGalsenDriver(request).getActiveSession(
				getSessionId(request));

		session.getDevice().runAdbCommand(
				"shell input keyevent " + payload.getString("keyCode"));
		return new GalsenResponse(getSessionId(request), "");
	}
}