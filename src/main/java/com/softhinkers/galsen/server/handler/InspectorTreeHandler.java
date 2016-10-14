package com.softhinkers.galsen.server.handler;

import java.nio.charset.Charset;
import java.util.logging.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.json.JSONException;

import com.softhinkers.galsen.exceptions.GalsenException;
import com.softhinkers.galsen.server.BaseGalsenServerHandler;
import com.softhinkers.galsen.server.JsResult;
import com.softhinkers.galsen.server.Response;
import com.softhinkers.galsen.server.UiResponse;
import com.softhinkers.galsen.server.http.HttpRequest;
import com.softhinkers.galsen.server.model.ActiveSession;
import com.softhinkers.galsen.server.util.HttpClientUtil;

public class InspectorTreeHandler extends BaseGalsenServerHandler {
	private static final Logger log;

	public InspectorTreeHandler(final String mappedUri) {
		super(mappedUri);
	}

	public Response handle(final HttpRequest request) throws JSONException {
		final String sessionId = this.getSessionId(request);
		InspectorTreeHandler.log.info("inspector tree handler, sessionId: "
				+ sessionId);
		ActiveSession session;
		if (sessionId == null || sessionId.isEmpty()) {
			if (this.getGalsenDriver(request).getActiveSessions() == null
					|| this.getGalsenDriver(request).getActiveSessions().size() < 1) {
				return (Response) new UiResponse(
						"",
						(Object) "Galsen inspector can only be used if there is an active test session running. To start a test session, add a break point into your test code and run the test in debug mode.");
			}
			session = this.getGalsenDriver(request).getActiveSessions().get(0);
			InspectorTreeHandler.log.info("Selected sessionId: "
					+ session.getSessionKey());
		} else {
			session = this.getGalsenDriver(request).getActiveSession(sessionId);
		}
		try {
			final HttpResponse r = HttpClientUtil.executeRequest(
					"http://localhost:" + session.getGalsenServerPort()
							+ "/inspector/tree", HttpMethod.GET);
			return (Response) new JsResult(EntityUtils.toString(r.getEntity(),
					Charset.forName("UTF-8")));
		} catch (Exception e) {
			e.printStackTrace();
			throw new GalsenException((Throwable) e);
		}
	}

	static {
		log = Logger.getLogger(InspectorTreeHandler.class.getName());
	}
}