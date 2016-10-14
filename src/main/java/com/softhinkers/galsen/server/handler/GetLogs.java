package com.softhinkers.galsen.server.handler;


import org.json.JSONArray;
import org.json.JSONException;

import com.softhinkers.galsen.log.LogEntry;
import com.softhinkers.galsen.server.BaseGalsenServerHandler;
import com.softhinkers.galsen.server.GalsenResponse;
import com.softhinkers.galsen.server.Response;
import com.softhinkers.galsen.server.http.HttpRequest;
import com.softhinkers.galsen.server.model.ActiveSession;

public class GetLogs extends BaseGalsenServerHandler {
	public GetLogs(String mappedUri) {
		super(mappedUri);
	}

	public Response handle(final HttpRequest request) throws JSONException {
		final ActiveSession session = this.getGalsenDriver(request)
				.getActiveSession(this.getSessionId(request));
		final JSONArray logs = new JSONArray();
		for (LogEntry l : session.getDevice().getLogs()) {
			logs.put((Object) l.toString());
		}
		return (Response) new GalsenResponse(this.getSessionId(request),
				(Object) logs);
	}
}
