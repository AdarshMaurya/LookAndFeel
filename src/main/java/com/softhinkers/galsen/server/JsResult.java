package com.softhinkers.galsen.server;


public class JsResult implements Response {
	private String result;

	public JsResult(String result) {
		this.result = result;
	}

	public String getSessionId() {
		return "";
	}

	public String render() {
		return this.result;
	}
}