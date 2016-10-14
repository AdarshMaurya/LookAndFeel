package com.softhinkers.galsen.server.handler;

import java.util.logging.Logger;

import org.json.JSONException;

import com.softhinkers.galsen.server.BaseGalsenServerHandler;
import com.softhinkers.galsen.server.Response;
import com.softhinkers.galsen.server.UiResponse;
import com.softhinkers.galsen.server.http.HttpRequest;
import com.softhinkers.galsen.server.inspector.BaseInspectorViewRenderer;
import com.softhinkers.galsen.server.model.ActiveSession;

public class InspectorUiHandler extends BaseGalsenServerHandler {
	private static final Logger log;

	public InspectorUiHandler(final String mappedUri) {
		super(mappedUri);
	}

	public Response handle(final HttpRequest request) throws JSONException {
		final String sessionId = this.getSessionId(request);
		InspectorUiHandler.log.info("inspector command, sessionId: "
				+ sessionId);
		ActiveSession session;
		if (sessionId == null || sessionId.isEmpty()) {
			if (this.getGalsenDriver(request).getActiveSessions() == null
					|| this.getGalsenDriver(request).getActiveSessions()
							.size() < 1) {
				return (Response) new UiResponse(
						"",
						(Object) "Galsen inspector can only be used if there is an active test session running. To start a test session, add a break point into your test code and run the test in debug mode.");
			}
			session = this.getGalsenDriver(request).getActiveSessions()
					.get(0);
			InspectorUiHandler.log.info("Selected sessionId: "
					+ session.getSessionKey());
		} else {
			if (!this.getGalsenDriver(request).isValidSession(sessionId)) {
				return (Response) new UiResponse(
						"",
						(Object) "You are using an invalid session key. Please open the inspector with the base uri: <IpAddress>:<Port>/inspector");
			}
			session = this.getGalsenDriver(request).getActiveSession(
					sessionId);
		}
		return (Response) new UiResponse((sessionId != null) ? sessionId : "",
				(Object) new MyInspectorViewRenderer(session)
						.buildHtml(request));
	}

	static {
		log = Logger.getLogger(InspectorUiHandler.class.getName());
	}

	public class MyInspectorViewRenderer extends BaseInspectorViewRenderer {
		private ActiveSession session;

		public MyInspectorViewRenderer(final ActiveSession session) {
			this.session = session;
		}

		public String getResource(final String name) {
			return "http://localhost:" + this.session.getGalsenServerPort()
					+ "/inspector/resources/" + name;
		}

		public String getScreen(final HttpRequest request) {
			return "http://" + request.header("Host") + "/inspector/session/"
					+ this.session.getSessionKey() + "/screenshot";
		}
	}
}