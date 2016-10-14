package com.softhinkers.galsen.server.handler;

import org.jboss.netty.handler.codec.http.HttpMethod;
import org.json.JSONException;
import org.json.JSONObject;

import com.softhinkers.galsen.android.AndroidDevice;
import com.softhinkers.galsen.android.impl.DefaultAndroidEmulator;
import com.softhinkers.galsen.server.BaseGalsenServerHandler;
import com.softhinkers.galsen.server.GalsenResponse;
import com.softhinkers.galsen.server.Response;
import com.softhinkers.galsen.server.http.HttpRequest;
import com.softhinkers.galsen.server.model.ActiveSession;
import com.softhinkers.galsen.server.util.HttpClientUtil;

public class NetworkConnectionHandler extends BaseGalsenServerHandler {
	public NetworkConnectionHandler(String mappedUri) {
		super(mappedUri);
	}

	public Response handle(HttpRequest request) throws JSONException {
		String sessionId = getSessionId(request);
		ActiveSession session = getGalsenDriver(request).getActiveSession(
				sessionId);

		String url = "http://localhost:" + session.getGalsenServerPort()
				+ request.uri();
		Integer connectionType = Integer.valueOf(getPayload(request)
				.getJSONObject("parameters").getInt("type"));
		try {
			JSONObject r = HttpClientUtil.parseJsonResponse(HttpClientUtil
					.executeRequest(url, HttpMethod.GET));

			if (r.getInt("value") % 2 == connectionType.intValue() % 2) {
				return new GalsenResponse(sessionId, null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new GalsenResponse(sessionId, null);
		}

		Boolean airplaneMode = Boolean
				.valueOf(connectionType.intValue() % 2 == 1);

		AndroidDevice device = getGalsenDriver(request).getActiveSession(
				getSessionId(request)).getDevice();

		int deviceAPILevel = Integer.parseInt(device.getTargetPlatform()
				.getApi());

		device.invokeActivity("android.settings.AIRPLANE_MODE_SETTINGS");

		device.runAdbCommand("shell input tap 600 100");

		device.inputKeyevent(20);

		device.inputKeyevent(23);

		if (airplaneMode.booleanValue()) {
			device.restartADB();

			for (ActiveSession activeSession : getGalsenDriver(request)
					.getActiveSessions())
				device.forwardPort(activeSession.getGalsenServerPort(),
						activeSession.getGalsenServerPort());
		} else if ((deviceAPILevel == 17)
				&& (device instanceof DefaultAndroidEmulator)) {
			device.runAdbCommand("shell svc data disable");
			device.runAdbCommand("shell svc data enable");
		}

		device.inputKeyevent(19);
		device.inputKeyevent(23);

		return new GalsenResponse(sessionId, Integer.valueOf((airplaneMode
				.booleanValue()) ? 1 : 6));
	}
}