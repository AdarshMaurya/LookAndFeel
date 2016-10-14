package com.softhinkers.galsen.server.http;

import java.util.Map;

public abstract interface HttpRequest {
	public abstract String method();

	public abstract String uri();

	public abstract String body();

	public abstract String header(String paramString);

	public abstract Map<String, Object> data();
}