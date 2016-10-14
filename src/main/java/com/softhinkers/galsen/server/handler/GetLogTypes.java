package com.softhinkers.galsen.server.handler;

import org.json.JSONArray;
import org.json.JSONException;

import com.softhinkers.galsen.server.BaseGalsenServerHandler;
import com.softhinkers.galsen.server.GalsenResponse;
import com.softhinkers.galsen.server.Response;
import com.softhinkers.galsen.server.http.HttpRequest;

public class GetLogTypes extends BaseGalsenServerHandler {
	public GetLogTypes(String mappedUri) {
		super(mappedUri);
	}

	public Response handle(HttpRequest request) throws JSONException {
		return new GalsenResponse(getSessionId(request), new JSONArray(
				"logcat"));
	}
}