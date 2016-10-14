package com.softhinkers.galsen.server;


public class UiResponse implements Response {
	private String sessionId;
	private Object object;

	public UiResponse(String sessionId, Object object) {
		this.sessionId = sessionId;
		this.object = object;
	}

	public String getSessionId() {
		return this.sessionId;
	}

	public Object getObject() {
		return this.object;
	}

	public String render() {
		if (this.object == null)
			return "";
		if (this.object instanceof String) {
			return ((String) this.object);
		}
		return this.object.toString();
	}
}