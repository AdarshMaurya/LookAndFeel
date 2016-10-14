package com.softhinkers.galsen.log;

import java.util.logging.Level;

public enum LogLevelEnum {
	ERROR, DEBUG, INFO, WARNING, VERBOSE;
	
	public Level level;
	public static LogLevelEnum fromString(String code) {
		for (final LogLevelEnum output : values()) {
			if (output.toString().equalsIgnoreCase(code)) {
				return output;
			}
		}
		return null;
		
	}
}
