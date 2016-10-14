package com.softhinkers.galsen.android;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.softhinkers.galsen.exceptions.AndroidDeviceException;

public class TelnetClient {
	Socket socket = null;
	PrintWriter out = null;
	BufferedReader in = null;

	public TelnetClient(Integer port) throws AndroidDeviceException {
		try {
			this.socket = new Socket("127.0.0.1", port.intValue());
			this.out = new PrintWriter(this.socket.getOutputStream(), true);
			this.in = new BufferedReader(new InputStreamReader(
					this.socket.getInputStream()));
			if (this.in.readLine() == null)
				throw new AndroidDeviceException(
						"Cannot establish a connection to device.");
		} catch (Exception e) {
			throw new AndroidDeviceException(
					"Cannot establish a connection to device.", e);
		}
	}

	public String sendCommand(String command) {
		try {
			sendQuietly(command);

			this.in.readLine();
			return this.in.readLine();
		} catch (Exception e) {
		}
		return "";
	}

	public void sendQuietly(String command) {
		try {
			this.out.write(command);
			this.out.write("\r\n");
			this.out.flush();
		} catch (Exception e) {
		}
	}

	public void close() {
		try {
			this.out.close();
		} catch (Exception e) {
		}
		try {
			this.in.close();
		} catch (Exception e) {
		}
		try {
			this.socket.close();
		} catch (Exception e) {
		}
	}
}