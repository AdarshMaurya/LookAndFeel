package com.softhinkers.galsen.server.handler;

import java.util.logging.Logger;

import org.json.JSONException;
import org.openqa.selenium.internal.Base64Encoder;

import com.softhinkers.galsen.exceptions.AndroidDeviceException;
import com.softhinkers.galsen.server.BaseGalsenServerHandler;
import com.softhinkers.galsen.server.GalsenResponse;
import com.softhinkers.galsen.server.Response;
import com.softhinkers.galsen.server.http.HttpRequest;

public class CaptureScreenshot extends BaseGalsenServerHandler {
	private static final Logger log = Logger.getLogger(CaptureScreenshot.class
			.getName());

	public CaptureScreenshot(String mappedUri) {
		super(mappedUri);
	}

	public Response handle(HttpRequest request) throws JSONException {
		log.info("take devcie screenshot command");
		byte[] rawPng;
		try {
			rawPng = getGalsenDriver(request).takeScreenshot(
					getSessionId(request));
		} catch (AndroidDeviceException e) {
			e.printStackTrace();
			return new GalsenResponse(getSessionId(request), 13, e);
		}
		String base64Png = new Base64Encoder().encode(rawPng);

		return new GalsenResponse(getSessionId(request), base64Png);
	}
}