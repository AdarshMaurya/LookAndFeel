package com.softhinkers.galsen.server.handler;

import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import com.softhinkers.galsen.server.BaseGalsenServerHandler;
import com.softhinkers.galsen.server.GalsenResponse;
import com.softhinkers.galsen.server.Response;
import com.softhinkers.galsen.server.http.HttpRequest;

public class CreateSessionHandler extends BaseGalsenServerHandler {
	private static final Logger log = Logger
			.getLogger(CreateSessionHandler.class.getName());

	public CreateSessionHandler(String mappedUri) {
		super(mappedUri);
	}

	public Response handle(HttpRequest request) throws JSONException {
		JSONObject payload = getPayload(request);
		log.info("new session command with capabilities: "
				+ payload.toString(2));

		JSONObject desiredCapabilities = payload
				.getJSONObject("desiredCapabilities");
		String sessionID;
		try {
			sessionID = getGalsenDriver(request).createNewTestSession(
					desiredCapabilities, Integer.valueOf(5));
		} catch (Exception e) {
			log.severe("Error while creating new session: " + e.getMessage());
			e.printStackTrace();
			return new GalsenResponse("", 33, e);
		}
		return new GalsenResponse(sessionID, 0, desiredCapabilities);
	}
}
