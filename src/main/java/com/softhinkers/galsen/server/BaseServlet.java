package com.softhinkers.galsen.server;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import com.softhinkers.galsen.server.http.HttpRequest;
import com.softhinkers.galsen.server.http.HttpResponse;
import com.softhinkers.galsen.server.http.HttpServlet;

public abstract class BaseServlet implements HttpServlet {
	public static final String SESSION_ID_KEY = "SESSION_ID_KEY";
	public static final String ELEMENT_ID_KEY = "ELEMENT_ID_KEY";
	public static final String COMMAND_NAME_KEY = "COMMAND_KEY";
	public static final String NAME_ID_KEY = "NAME_ID_KEY";
	public static final String DRIVER_KEY = "DRIVER_KEY";
	public static final int INTERNAL_SERVER_ERROR = 500;
	protected Map<String, BaseRequestHandler> getHandler;
	protected Map<String, BaseRequestHandler> postHandler;
	protected Map<String, BaseRequestHandler> deleteHandler;
	private Map<String, String[]> mapperUrlSectionsCache;

	public BaseServlet() {
		this.getHandler = new HashMap();
		this.postHandler = new HashMap();
		this.deleteHandler = new HashMap();

		this.mapperUrlSectionsCache = new HashMap();
	}

	protected BaseRequestHandler findMatcher(HttpRequest request,
			Map<String, BaseRequestHandler> handler) {
		String[] urlToMatchSections = getRequestUrlSections(request.uri());
		for (Map.Entry entry : handler.entrySet()) {
			String[] mapperUrlSections = getMapperUrlSectionsCached((String) entry
					.getKey());
			if (isFor(mapperUrlSections, urlToMatchSections)) {
				return ((BaseRequestHandler) entry.getValue());
			}
		}
		return null;
	}

	protected abstract void init();

	public void handleHttpRequest(HttpRequest request, HttpResponse response)
			throws Exception {
		BaseRequestHandler handler = null;
		if ("GET".equals(request.method()))
			handler = findMatcher(request, this.getHandler);
		else if ("POST".equals(request.method()))
			handler = findMatcher(request, this.postHandler);
		else if ("DELETE".equals(request.method())) {
			handler = findMatcher(request, this.deleteHandler);
		}
		handleRequest(request, response, handler);
	}

	protected void register(Map<String, BaseRequestHandler> registerOn,
			BaseRequestHandler handler) {
		registerOn.put(handler.getMappedUri(), handler);
	}

	public abstract void handleRequest(HttpRequest paramHttpRequest,
			HttpResponse paramHttpResponse,
			BaseRequestHandler paramBaseRequestHandler);

	protected String getParameter(String configuredUri, String actualUri,
			String param) {
		return getParameter(configuredUri, actualUri, param, true);
	}

	protected String getParameter(String configuredUri, String actualUri,
			String param, boolean sectionLengthValidation) {
		String[] configuredSections = configuredUri.split("/");
		String[] currentSections = actualUri.split("/");
		if ((sectionLengthValidation)
				&& (configuredSections.length != currentSections.length)) {
			return null;
		}

		for (int i = 0; i < currentSections.length; ++i) {
			if (configuredSections[i].contains(param)) {
				return currentSections[i];
			}
		}
		return null;
	}

	protected void replyWithServerError(HttpResponse response) {
		System.out.println("replyWithServerError 500");
		response.setStatus(500);
		response.end();
	}

	protected boolean isFor(String[] mapperUrlSections,
			String[] urlToMatchSections) {
		if (urlToMatchSections == null) {
			return (mapperUrlSections.length == 0);
		}
		if (mapperUrlSections.length != urlToMatchSections.length) {
			return false;
		}
		for (int i = 0; i < mapperUrlSections.length; ++i) {
			if ((!(mapperUrlSections[i].startsWith(":")))
					&& (!(mapperUrlSections[i].equals(urlToMatchSections[i])))) {
				return false;
			}
		}
		return true;
	}

	protected boolean isNewSessionRequest(HttpRequest request) {
		return (("POST".equals(request.method())) && ("/wd/hub/session"
				.equals(request.uri())));
	}

	protected void handleResponse(HttpRequest request, HttpResponse response,
			GalsenResponse result) {
		response.setContentType("application/json");
		response.setEncoding(Charset.forName("UTF-8"));
		if (result != null) {
			String resultString = result.render();
			response.setContent(resultString);
		}
		if ((isNewSessionRequest(request)) && (result.getStatus() == 0)) {
			String session = result.getSessionId();

			String newSessionUri = "http://" + request.header("Host")
					+ request.uri() + "/" + session;
			System.out.println("new Session URL: " + newSessionUri);
			response.sendRedirect(newSessionUri);
		} else {
			response.setStatus(200);
		}
		response.end();
	}

	private String[] getRequestUrlSections(String urlToMatch) {
		if (urlToMatch == null) {
			return null;
		}
		int qPos = urlToMatch.indexOf(63);
		if (qPos != -1) {
			urlToMatch = urlToMatch.substring(0, urlToMatch.indexOf("?"));
		}
		return urlToMatch.split("/");
	}

	private String[] getMapperUrlSectionsCached(String mapperUrl) {
		String[] sections = (String[]) this.mapperUrlSectionsCache
				.get(mapperUrl);
		if (sections == null) {
			sections = mapperUrl.split("/");
			for (int i = 0; i < sections.length; ++i) {
				String section = sections[i];

				int qPos = section.indexOf(63);
				if (qPos != -1) {
					sections[i] = section.substring(0, qPos);
				}
			}
			this.mapperUrlSectionsCache.put(mapperUrl, sections);
		}
		return sections;
	}
}