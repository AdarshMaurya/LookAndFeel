package com.softhinkers.galsen.server;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.json.JSONException;
import org.json.JSONObject;

public class GalsenResponse implements Response {
	private String sessionId;
	private int status;
	private Object value;

	protected GalsenResponse() {
	}

	public GalsenResponse(String sessionId, int status, JSONObject value) {
		this.sessionId = sessionId;
		this.status = status;
		this.value = value;
	}

	public GalsenResponse(String sessionId, int status, Exception e)
			throws JSONException {
		this.value = buildErrorValue(e);
		this.sessionId = sessionId;
		this.status = status;
	}

	public GalsenResponse(String sessionId, Object value) {
		this.sessionId = sessionId;
		this.status = 0;
		this.value = value;
	}

	public GalsenResponse(String sessionId, int status, Object value) {
		this.sessionId = sessionId;
		this.status = status;
		this.value = value;
	}

	public String getSessionId() {
		return this.sessionId;
	}

	public int getStatus() {
		return this.status;
	}

	public Object getValue() {
		return this.value;
	}

	public String render() {
		JSONObject o = new JSONObject();
		try {
			if (this.sessionId != null) {
				o.put("sessionId", this.sessionId);
			}
			o.put("status", this.status);
			if (this.value != null)
				o.put("value", this.value);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return o.toString();
	}

	private JSONObject buildErrorValue(Throwable t) throws JSONException {
		JSONObject errorValue = new JSONObject();
		errorValue.put("class", t.getClass().getCanonicalName());

		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		t.printStackTrace(printWriter);
		errorValue.put("message",
				t.getMessage() + "\n" + stringWriter.toString());

		return errorValue;
	}

}
