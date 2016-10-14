package com.softhinkers.galsen.server.handler;

import java.util.logging.Logger;

import org.apache.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.json.JSONException;
import org.json.JSONObject;

import com.softhinkers.galsen.android.AndroidDevice;
import com.softhinkers.galsen.exceptions.GalsenException;
import com.softhinkers.galsen.log.LogEntry;
import com.softhinkers.galsen.server.BaseGalsenServerHandler;
import com.softhinkers.galsen.server.GalsenResponse;
import com.softhinkers.galsen.server.Response;
import com.softhinkers.galsen.server.http.HttpRequest;
import com.softhinkers.galsen.server.model.ActiveSession;
import com.softhinkers.galsen.server.util.HttpClientUtil;



public class RequestRedirectHandler extends BaseGalsenServerHandler {
	private static final Logger log;

	public RequestRedirectHandler(final String mappedUri) {
		super(mappedUri);
	}

	public Response handle(final HttpRequest request) throws JSONException {
		final String sessionId = this.getSessionId(request);
		RequestRedirectHandler.log.info("forward request command: for session "
				+ sessionId);
		final ActiveSession session = this.getGalsenDriver(request)
				.getActiveSession(sessionId);
		if (session == null) {
			return (Response) new GalsenResponse(sessionId, 13,
					(Exception) new GalsenException(
							"No session found for given sessionId: "
									+ sessionId));
		}
		if (session.isInvalid()) {
			return (Response) new GalsenResponse(
					sessionId,
					13,
					(Exception) new GalsenException(
							"The test session has been marked as invalid. This happens if a hardware device was disconnected but a test session was still active on the device."));
		}
		final String url = "http://localhost:"
				+ session.getGalsenServerPort() + request.uri();
		final String method = request.method();
		JSONObject response = null;
		int retries = 3;
		while (retries-- > 0) {
			try {
				response = this.redirectRequest(request, session, url, method);
			} catch (Exception e) {
				if (retries == 0) {
					final AndroidDevice device = session.getDevice();
					RequestRedirectHandler.log.info("getting logs");
					device.setVerbose();
					for (final LogEntry le : device.getLogs()) {
						System.out.println(le.getMessage());
					}
					return (Response) new GalsenResponse(
							sessionId,
							13,
							(Exception) new GalsenException(
									"Error occured while communicating with selendroid server on the device: ",
									(Throwable) e));
				}
				RequestRedirectHandler.log
						.severe("failed to forward request to Selendroid Server");
				continue;
			}
			break;
		}
		final Object value = response.opt("value");
		if (value != null) {
			String displayed = String.valueOf(value);
			if (displayed.length() > 160) {
				displayed = displayed.substring(0, 157) + "...";
			}
			RequestRedirectHandler.log
					.info("return value from selendroid android server: "
							+ displayed);
		}
		final int status = response.getInt("status");
		RequestRedirectHandler.log
				.fine("return value from selendroid android server: " + value);
		RequestRedirectHandler.log
				.fine("return status from selendroid android server: " + status);
		return (Response) new GalsenResponse(sessionId, status, value);
	}

	private JSONObject redirectRequest(final HttpRequest request,
			final ActiveSession session, final String url, final String method)
			throws Exception, JSONException {
		HttpResponse r = null;
		if ("get".equalsIgnoreCase(method)) {
			RequestRedirectHandler.log.info("GET redirect to: " + url);
			r = HttpClientUtil.executeRequest(url, HttpMethod.GET);
		} else if ("post".equalsIgnoreCase(method)) {
			RequestRedirectHandler.log.info("POST redirect to: " + url);
			final JSONObject payload = this.getPayload(request);
			RequestRedirectHandler.log.info("Payload? " + payload);
			r = HttpClientUtil.executeRequestWithPayload(url,
					session.getGalsenServerPort(), HttpMethod.POST,
					payload.toString());
		} else {
			if (!"delete".equalsIgnoreCase(method)) {
				throw new GalsenException("Http method not supported.");
			}
			RequestRedirectHandler.log.info("DELETE redirect to: " + url);
			r = HttpClientUtil.executeRequest(url, HttpMethod.DELETE);
		}
		return HttpClientUtil.parseJsonResponse(r);
	}

	static {
		log = Logger.getLogger(RequestRedirectHandler.class.getName());
	}
}