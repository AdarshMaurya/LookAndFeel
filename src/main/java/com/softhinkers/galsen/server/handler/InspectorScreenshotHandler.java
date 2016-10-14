package com.softhinkers.galsen.server.handler;

import java.util.logging.Logger;

import org.json.JSONException;

import com.softhinkers.galsen.exceptions.AndroidDeviceException;
import com.softhinkers.galsen.server.BaseGalsenServerHandler;
import com.softhinkers.galsen.server.Response;
import com.softhinkers.galsen.server.UiResponse;
import com.softhinkers.galsen.server.http.HttpRequest;
import com.softhinkers.galsen.server.model.ActiveSession;

public class InspectorScreenshotHandler extends BaseGalsenServerHandler {
	private static final Logger log;

	public InspectorScreenshotHandler(final String mappedUri) {
		super(mappedUri);
	}

	public Response handle(final HttpRequest request) throws JSONException {
		final String sessionId = this.getSessionId(request);
		InspectorScreenshotHandler.log
				.info("inspector screenshot handler, sessionId: " + sessionId);
		if (sessionId == null || sessionId.isEmpty()) {
			if (this.getGalsenDriver(request).getActiveSessions() == null
					|| this.getGalsenDriver(request).getActiveSessions()
							.size() < 1) {
				return (Response) new UiResponse(
						"",
						(Object) "Selendroid inspector can only be used if there is an active test session running. To start a test session, add a break point into your test code and run the test in debug mode.");
			}
			final ActiveSession session = this.getGalsenDriver(request)
					.getActiveSessions().get(0);
			InspectorScreenshotHandler.log.info("Selected sessionId: "
					+ session.getSessionKey());
		} else {
			final ActiveSession session = this.getGalsenDriver(request)
					.getActiveSession(sessionId);
		}
		byte[] screenshot = null;
		try {
			screenshot = this.getGalsenDriver(request).takeScreenshot(
					sessionId);
		} catch (AndroidDeviceException e) {
			e.printStackTrace();
		}
		return (Response) new UiResponse((sessionId != null) ? sessionId : "",
				(Object) screenshot);
	}

	static {
		log = Logger.getLogger(InspectorScreenshotHandler.class.getName());
	}
}