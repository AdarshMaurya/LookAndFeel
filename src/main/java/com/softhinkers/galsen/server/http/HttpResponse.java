package com.softhinkers.galsen.server.http;

import java.nio.charset.Charset;

public abstract interface HttpResponse {
	public abstract HttpResponse setStatus(int paramInt);

	public abstract HttpResponse setContentType(String paramString);

	public abstract HttpResponse setContent(byte[] paramArrayOfByte);

	public abstract HttpResponse setContent(String paramString);

	public abstract HttpResponse setEncoding(Charset paramCharset);

	public abstract HttpResponse sendRedirect(String paramString);

	public abstract HttpResponse sendTemporaryRedirect(String paramString);

	public abstract void end();

	public abstract boolean isClosed();
}