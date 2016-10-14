package com.softhinkers.galsen.server.http;

public abstract interface HttpServlet  {
	public abstract void handleHttpRequest(HttpRequest paramHttpRequest,
			HttpResponse paramHttpResponse) throws Exception;
}
