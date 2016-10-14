package com.softhinkers.galsen.server.handler;

import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import com.softhinkers.galsen.GalsenCapabilities;
import com.softhinkers.galsen.exceptions.GalsenException;
import com.softhinkers.galsen.server.BaseGalsenServerHandler;
import com.softhinkers.galsen.server.GalsenResponse;
import com.softhinkers.galsen.server.Response;
import com.softhinkers.galsen.server.http.HttpRequest;


public class GetCapabilities extends BaseGalsenServerHandler {
	private static final Logger log = Logger.getLogger(GetCapabilities.class
			.getName());

	public GetCapabilities(String mappedUri) {
		super(mappedUri);
	}

	public Response handle(HttpRequest request) throws JSONException {
		log.info("get capabilities command");
		String sessionId = getSessionId(request);

	GalsenCapabilities caps = getGalsenDriver(request)
				.getSessionCapabilities(sessionId);
		if (caps == null) {
			return new GalsenResponse(sessionId, 13,
					new GalsenException("Session was not found"));
		}
		return new GalsenResponse(sessionId, new JSONObject(caps.asMap()));
	}
}