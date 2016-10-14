package com.softhinkers.galsen.server;

import com.softhinkers.galsen.server.http.HttpRequest;
import com.softhinkers.galsen.server.model.GalsenStandaloneDriver;

public abstract class BaseGalsenServerHandler extends BaseRequestHandler {
	public BaseGalsenServerHandler(String mappedUri) {
		super(mappedUri);
	}

	protected GalsenStandaloneDriver getGalsenDriver(HttpRequest request) {
		return ((GalsenStandaloneDriver) request.data().get("DRIVER_KEY"));
	}
}