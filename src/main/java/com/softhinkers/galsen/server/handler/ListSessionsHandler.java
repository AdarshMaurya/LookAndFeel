package com.softhinkers.galsen.server.handler;

import java.util.List;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.softhinkers.galsen.server.BaseGalsenServerHandler;
import com.softhinkers.galsen.server.GalsenResponse;
import com.softhinkers.galsen.server.Response;
import com.softhinkers.galsen.server.http.HttpRequest;
import com.softhinkers.galsen.server.model.ActiveSession;

public class ListSessionsHandler  extends BaseGalsenServerHandler {
	private static final Logger log;

	public ListSessionsHandler(final String mappedUri) {
		super(mappedUri);
	}

	public Response handle(final HttpRequest request) throws JSONException {
		ListSessionsHandler.log.info("list sessions command");
		final JSONArray sessions = new JSONArray();
		final List<ActiveSession> activeSessions = (List<ActiveSession>) this
				.getGalsenDriver(request).getActiveSessions();
		if (activeSessions != null && !activeSessions.isEmpty()) {
			for (final ActiveSession session : activeSessions) {
				final JSONObject sessionResponse = new JSONObject();
				sessionResponse.put("id", (Object) session.getSessionKey());
				sessionResponse.put("capabilities", (Object) new JSONObject(
						session.getDesiredCapabilities().asMap()));
				sessions.put((Object) sessionResponse);
			}
		}
		return (Response) new GalsenResponse((String) null,
				(Object) sessions);
	}

	static {
		log = Logger.getLogger(ListSessionsHandler.class.getName());
	}
}