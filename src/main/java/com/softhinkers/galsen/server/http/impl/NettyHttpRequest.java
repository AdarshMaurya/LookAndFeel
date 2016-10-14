package com.softhinkers.galsen.server.http.impl;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.CharsetUtil;

import java.util.HashMap;
import java.util.Map;

import com.softhinkers.galsen.server.http.HttpRequest;

public class NettyHttpRequest implements HttpRequest {
	private FullHttpRequest request;
	private Map<String, Object> data;

	public NettyHttpRequest(final FullHttpRequest reuqest) {
		this.request = reuqest;
		this.data = new HashMap<String, Object>();
	}

	public String method() {
		return this.request.getMethod().name();
	}

	public String uri() {
		return this.request.getUri();
	}

	public String body() {
		return this.request.content().toString(CharsetUtil.UTF_8);
	}

	public String header(final String name) {
		return this.request.headers().get(name);
	}

	public Map<String, Object> data() {
		return this.data;
	}
}