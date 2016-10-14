package com.softhinkers.galsen.io;

import java.util.Map;
import java.util.logging.Logger;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.LogOutputStream;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.environment.EnvironmentUtils;

import com.softhinkers.galsen.exceptions.ShellCommandException;

public class ShellCommand {
	private static final Logger log = Logger.getLogger(ShellCommand.class
			.getName());

	public static String exec(CommandLine commandLine)
			throws ShellCommandException {
		return exec(commandLine, 20000L);
	}

	public static String exec(CommandLine commandline, long timeoutInMillies)
			throws ShellCommandException {
		log.info("executing command: " + commandline);
		ShellCommand.PritingLogOutputStream outputStream = new ShellCommand.PritingLogOutputStream();
		DefaultExecutor exec = new DefaultExecutor();
		exec.setWatchdog(new ExecuteWatchdog(timeoutInMillies));
		PumpStreamHandler streamHandler = new PumpStreamHandler(outputStream);
		exec.setStreamHandler(streamHandler);

		try {
			exec.execute(commandline);
		} catch (Exception arg6) {
			throw new ShellCommandException(
					"An error occured while executing shell command: "
							+ commandline, new ShellCommandException(
							outputStream.getOutput()));
		}

		return outputStream.getOutput();
	}

	public static void execAsync(CommandLine commandline)
			throws ShellCommandException {
		execAsync((String) null, commandline);
	}

	public static void execAsync(String display, CommandLine commandline)
			throws ShellCommandException {
		log.info("executing async command: " + commandline);
		DefaultExecutor exec = new DefaultExecutor();
		DefaultExecuteResultHandler handler = new DefaultExecuteResultHandler();
		PumpStreamHandler streamHandler = new PumpStreamHandler(
				new ShellCommand.PritingLogOutputStream());
		exec.setStreamHandler(streamHandler);

		try {
			if (display != null && !display.isEmpty()) {
				Map e = EnvironmentUtils.getProcEnvironment();
				EnvironmentUtils.addVariableToEnvironment(e, "DISPLAY=:"
						+ display);
				exec.execute(commandline, e, handler);
			} else {
				exec.execute(commandline, handler);
			}

		} catch (Exception arg5) {
			throw new ShellCommandException(
					"An error occured while executing shell command: "
							+ commandline, arg5);
		}
	}

	private static class PritingLogOutputStream extends LogOutputStream {
		private StringBuilder output;

		private PritingLogOutputStream() {
			this.output = new StringBuilder();
		}

		protected void processLine(String line, int level) {
			ShellCommand.log.fine("OUTPUT FROM PROCESS: " + line);
			this.output.append(line).append("\n");
		}

		public String getOutput() {
			return this.output.toString();
		}
	}
}