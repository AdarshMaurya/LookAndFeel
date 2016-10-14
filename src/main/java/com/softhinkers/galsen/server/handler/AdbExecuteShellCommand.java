package com.softhinkers.galsen.server.handler;

import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import com.softhinkers.galsen.server.BaseGalsenServerHandler;
import com.softhinkers.galsen.server.GalsenResponse;
import com.softhinkers.galsen.server.Response;
import com.softhinkers.galsen.server.http.HttpRequest;
import com.softhinkers.galsen.server.model.ActiveSession;

public class AdbExecuteShellCommand extends BaseGalsenServerHandler {
	private static final Logger log = Logger
			.getLogger(AdbExecuteShellCommand.class.getName());

	public AdbExecuteShellCommand(String mappedUri) {
		super(mappedUri);
	}

	public Response handle(HttpRequest request) throws JSONException {
		JSONObject payload = getPayload(request);
		log.info("execute shell command via adb");
		ActiveSession session = getGalsenDriver(request).getActiveSession(
				getSessionId(request));
		String command = "shell " + payload.getString("command");
		session.getDevice().runAdbCommand(command);
		return new GalsenResponse(getSessionId(request), "");
	}
}