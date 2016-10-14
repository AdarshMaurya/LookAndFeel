package com.softhinkers.galsen.log;

import com.beust.jcommander.IStringConverter;
import com.beust.jcommander.ParameterException;

public class LogLevelConverter implements IStringConverter<LogLevelEnum> {

	@Override
	public LogLevelEnum convert(String value) {
		LogLevelEnum convertedValue = LogLevelEnum.fromString(value);
		if (convertedValue == null) {
			throw new ParameterException(
					"Value "
							+ value
							+ "can not be converted to LogLevelEnum. "
							+ "Available values are: ERROR, WARNING, INFO, DEBUG and VERBOSE.");
		}
		return convertedValue;
	}

}
