package com.softhinkers.galsen.server.handler;

import java.util.logging.Logger;

import org.json.JSONException;

import com.softhinkers.galsen.exceptions.AndroidDeviceException;
import com.softhinkers.galsen.server.BaseGalsenServerHandler;
import com.softhinkers.galsen.server.GalsenResponse;
import com.softhinkers.galsen.server.Response;
import com.softhinkers.galsen.server.http.HttpRequest;
import com.softhinkers.galsen.server.model.GalsenStandaloneDriver;

public class DeleteSessionHandler extends BaseGalsenServerHandler {
	private static final Logger log = Logger
			.getLogger(DeleteSessionHandler.class.getName());

	public DeleteSessionHandler(String mappedUri) {
		super(mappedUri);
	}

	public Response handle(HttpRequest request) throws JSONException {
		log.info("delete session command");
		GalsenStandaloneDriver driver = getGalsenDriver(request);
		String sessionId = getSessionId(request);
		try {
			driver.stopSession(sessionId);
		} catch (AndroidDeviceException e) {
			log.severe("Error occured while stopping the emulator.");
		}

		return new GalsenResponse(sessionId, "");
	}
}