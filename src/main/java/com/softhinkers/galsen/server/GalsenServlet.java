package com.softhinkers.galsen.server;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.softhinkers.galsen.GalsenConfiguration;
import com.softhinkers.galsen.server.handler.AdbExecuteShellCommand;
import com.softhinkers.galsen.server.handler.AdbSendKeyEvent;
import com.softhinkers.galsen.server.handler.AdbSendText;
import com.softhinkers.galsen.server.handler.AdbTap;
import com.softhinkers.galsen.server.handler.CaptureScreenshot;
import com.softhinkers.galsen.server.handler.CreateSessionHandler;
import com.softhinkers.galsen.server.handler.DeleteSessionHandler;
import com.softhinkers.galsen.server.handler.GetCapabilities;
import com.softhinkers.galsen.server.handler.GetLogTypes;
import com.softhinkers.galsen.server.handler.GetLogs;
import com.softhinkers.galsen.server.handler.InspectorScreenshotHandler;
import com.softhinkers.galsen.server.handler.InspectorTreeHandler;
import com.softhinkers.galsen.server.handler.InspectorUiHandler;
import com.softhinkers.galsen.server.handler.ListSessionsHandler;
import com.softhinkers.galsen.server.handler.NetworkConnectionHandler;
import com.softhinkers.galsen.server.handler.RequestRedirectHandler;
import com.softhinkers.galsen.server.http.HttpRequest;
import com.softhinkers.galsen.server.http.HttpResponse;
import com.softhinkers.galsen.server.model.GalsenStandaloneDriver;

public class GalsenServlet extends BaseServlet {
	private static final Logger log;
	protected Map<String, BaseRequestHandler> redirectHandler;
	private GalsenStandaloneDriver driver;
	private GalsenConfiguration conf;

	public GalsenServlet(final GalsenStandaloneDriver driver,
			final GalsenConfiguration conf) {
		this.redirectHandler = new HashMap<String, BaseRequestHandler>();
		this.driver = driver;
		this.conf = conf;
		this.init();
	}

	protected void init() {
		this.register(
				this.postHandler,
				(BaseRequestHandler) new CreateSessionHandler("/wd/hub/session"));
		this.register(
				this.getHandler,
				(BaseRequestHandler) new ListSessionsHandler("/wd/hub/sessions"));
		this.register(this.getHandler,
				(BaseRequestHandler) new GetCapabilities(
						"/wd/hub/session/:sessionId"));
		this.register(this.getHandler, (BaseRequestHandler) new GetLogTypes(
				"/wd/hub/session/:sessionId/log/types"));
		this.register(this.postHandler, (BaseRequestHandler) new GetLogs(
				"/wd/hub/session/:sessionId/log"));
		if (!this.conf.isDeviceScreenshot()) {
			this.register(this.getHandler,
					(BaseRequestHandler) new CaptureScreenshot(
							"/wd/hub/session/:sessionId/screenshot"));
		}
		this.register(this.getHandler,
				(BaseRequestHandler) new InspectorTreeHandler(
						"/inspector/session/:sessionId/tree"));
		this.register(this.getHandler,
				(BaseRequestHandler) new InspectorScreenshotHandler(
						"/inspector/session/:sessionId/screenshot"));
		this.register(this.getHandler,
				(BaseRequestHandler) new InspectorUiHandler(
						"/inspector/session/:sessionId"));
		this.register(this.deleteHandler,
				(BaseRequestHandler) new DeleteSessionHandler(
						"/wd/hub/session/:sessionId"));
		this.register((Map) this.redirectHandler,
				(BaseRequestHandler) new RequestRedirectHandler(
						"/wd/hub/session/"));
		this.register(this.postHandler, (BaseRequestHandler) new GetLogs(
				"/wd/hub/session/:sessionId/log"));
		this.register(this.postHandler,
				(BaseRequestHandler) new AdbSendKeyEvent(
						"/wd/hub/-selendroid/:sessionId/adb/sendKeyEvent"));
		this.register(this.postHandler, (BaseRequestHandler) new AdbSendText(
				"/wd/hub/-selendroid/:sessionId/adb/sendText"));
		this.register(this.postHandler, (BaseRequestHandler) new AdbTap(
				"/wd/hub/-selendroid/:sessionId/adb/tap"));
		this.register(
				this.postHandler,
				(BaseRequestHandler) new AdbExecuteShellCommand(
						"/wd/hub/-selendroid/:sessionId/adb/executeShellCommand"));
		this.register(this.postHandler,
				(BaseRequestHandler) new NetworkConnectionHandler(
						"/wd/hub/session/:sessionId/network_connection"));
	}

	public void handleRequest(final HttpRequest request,
			final HttpResponse response, final BaseRequestHandler foundHandler) {
		BaseRequestHandler handler = null;
		if ("/favicon.ico".equals(request.uri()) && foundHandler == null) {
			response.setStatus(404);
			response.end();
			return;
		}
		if (!"/inspector/".equals(request.uri())
				&& !"/inspector".equals(request.uri())) {
			if (foundHandler == null) {
				if (!this.redirectHandler.isEmpty()) {
					for (final Map.Entry<String, BaseRequestHandler> entry : this.redirectHandler
							.entrySet()) {
						if (request.uri().startsWith(entry.getKey())) {
							final String sessionId = this.getParameter(
									"/wd/hub/session/:sessionId",
									request.uri(), ":sessionId", false);
							handler = entry.getValue();
							if (!this.driver.isValidSession(sessionId)) {
								continue;
							}
							request.data().put("SESSION_ID_KEY", sessionId);
						}
					}
				}
				if (handler == null) {
					response.setStatus(404);
					response.end();
					return;
				}
			} else {
				handler = foundHandler;
			}
			final String sessionId2 = this.getParameter(handler.getMappedUri(),
					request.uri(), ":sessionId");
			if (sessionId2 != null) {
				request.data().put("SESSION_ID_KEY", sessionId2);
			}
			request.data().put("DRIVER_KEY", this.driver);
			Response result;
			try {
				result = handler.handle(request);
			} catch (Exception e) {
				e.printStackTrace();
				GalsenServlet.log
						.severe("Error occurred while handling request: "
								+ e.fillInStackTrace());
				this.replyWithServerError(response);
				return;
			}
			if (result instanceof GalsenResponse) {
				this.handleResponse(request, response,
						(GalsenResponse) result);
			} else if (result instanceof JsResult) {
				final JsResult js = (JsResult) result;
				response.setContentType("application/x-javascript");
				response.setEncoding(Charset.forName("UTF-8"));
				response.setContent(js.render());
				response.end();
			} else {
				final UiResponse uiResponse = (UiResponse) result;
				response.setEncoding(Charset.forName("UTF-8"));
				response.setStatus(200);
				if (uiResponse != null) {
					if (uiResponse.getObject() instanceof byte[]) {
						response.setContentType("image/png");
						final byte[] data = (byte[]) uiResponse.getObject();
						response.setContent(data);
					} else {
						response.setContentType("text/html");
						final String resultString = uiResponse.render();
						response.setContent(resultString);
					}
				}
				response.end();
			}
			return;
		}
		if (this.driver.getActiveSessions().isEmpty()) {
			response.setStatus(200);
			response.setContent("Selendroid inspector can only be used if there is an active test session running. To start a test session, add a break point into your test code and run the test in debug mode.");
			response.end();
			return;
		}
		final String session = this.driver.getActiveSessions().get(0)
				.getSessionKey();
		final String newSessionUri = "http://" + request.header("Host")
				+ "/inspector/session/" + session + "/";
		GalsenServlet.log.info("new Inspector URL: " + newSessionUri);
		response.sendTemporaryRedirect(newSessionUri);
		response.end();
	}

	static {
		log = Logger.getLogger(GalsenServlet.class.getName());
	}
}