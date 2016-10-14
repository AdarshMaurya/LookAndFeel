package com.softhinkers.galsen.server.http.impl;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;

import java.nio.charset.Charset;

import com.softhinkers.galsen.server.http.HttpResponse;

public class NettyHttpResponse implements HttpResponse {
	private final FullHttpResponse response;
	private boolean closed;
	private Charset charset;

	public NettyHttpResponse(final FullHttpResponse response) {
		this.closed = false;
		this.charset = CharsetUtil.UTF_8;
		this.response = response;
		response.headers().add("Content-Encoding", (Object) "identity");
	}

	public HttpResponse setStatus(final int status) {
		this.response.setStatus(HttpResponseStatus.valueOf(status));
		return (HttpResponse) this;
	}

	public HttpResponse setContentType(final String mimeType) {
		this.response.headers().add("Content-Type", (Object) mimeType);
		return (HttpResponse) this;
	}

	public HttpResponse setContent(final byte[] data) {
		this.response.headers().add("Content-Length", (Object) data.length);
		this.response.content().writeBytes(data);
		return (HttpResponse) this;
	}

	public HttpResponse setContent(final String message) {
		this.setContent(message.getBytes(this.charset));
		return (HttpResponse) this;
	}

	public HttpResponse sendRedirect(final String to) {
		this.setStatus(301);
		this.response.headers().add("location", (Object) to);
		return (HttpResponse) this;
	}

	public HttpResponse sendTemporaryRedirect(final String to) {
		this.setStatus(302);
		this.response.headers().add("location", (Object) to);
		return (HttpResponse) this;
	}

	public void end() {
		this.closed = true;
	}

	public boolean isClosed() {
		return this.closed;
	}

	public HttpResponse setEncoding(final Charset charset) {
		this.charset = charset;
		return (HttpResponse) this;
	}
}