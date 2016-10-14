package com.softhinkers.galsen.server.handler;

import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import com.softhinkers.galsen.server.BaseGalsenServerHandler;
import com.softhinkers.galsen.server.GalsenResponse;
import com.softhinkers.galsen.server.Response;
import com.softhinkers.galsen.server.http.HttpRequest;
import com.softhinkers.galsen.server.model.ActiveSession;

public class AdbTap extends BaseGalsenServerHandler {
	private static final Logger log = Logger.getLogger(AdbTap.class.getName());

	public AdbTap(String mappedUri) {
		super(mappedUri);
	}

	public Response handle(HttpRequest request) throws JSONException {
		JSONObject payload = getPayload(request);
		log.info("Send tap Event via adb: " + payload.toString(2));
		ActiveSession session = getGalsenDriver(request).getActiveSession(
				getSessionId(request));
		String command = String.format("shell input tap %s %s", new Object[] {
				payload.getString("x"), payload.getString("y") });

		session.getDevice().runAdbCommand(command);
		return new GalsenResponse(getSessionId(request), "");
	}
}
