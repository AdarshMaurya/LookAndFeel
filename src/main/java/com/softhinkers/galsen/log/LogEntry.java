package com.softhinkers.galsen.log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class LogEntry {

	private static final ThreadLocal<SimpleDateFormat> DATE_FORMAT = new ThreadLocal() {
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		}
	};
	private final Level level;
	private final long timestamp;
	private final String message;

	public LogEntry(Level level, long timestamp, String message) {
		this.level = level;
		this.timestamp = timestamp;
		this.message = message;
	}

	public Level getLevel() {
		return this.level;
	}

	public long getTimestamp() {
		return this.timestamp;
	}

	public String getMessage() {
		return this.message;
	}

	public String toString() {
		return String.format(
				"[%s] [%s] %s",
				new Object[] {
						((SimpleDateFormat) DATE_FORMAT.get()).format(new Date(
								this.timestamp)), this.level, this.message });
	}

	public Map<String, Object> toMap() {
		Map map = new HashMap();
		map.put("timestamp", Long.valueOf(this.timestamp));
		map.put("level", this.level);
		map.put("message", this.message);
		return map;
	}
}