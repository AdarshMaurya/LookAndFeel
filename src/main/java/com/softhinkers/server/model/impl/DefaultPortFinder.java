package com.softhinkers.server.model.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.softhinkers.galsen.server.model.EmulatorPortFinder;

public class DefaultPortFinder implements EmulatorPortFinder {
	private List<Integer> availablePorts = new ArrayList();
	private List<Integer> portsInUse = new ArrayList();
	private Integer minPort;
	private Integer maxPort;

	public DefaultPortFinder(Integer minPort, Integer maxPort) {
		this.minPort = minPort;
		this.maxPort = maxPort;
		for (int i = minPort.intValue(); i <= maxPort.intValue(); ++i)
			if (isEvenNumber(Integer.valueOf(i)))
				this.availablePorts.add(Integer.valueOf(i));
	}

	public synchronized Integer next() {
		if (this.availablePorts.isEmpty()) {
			return null;
		}
		Collections.sort(this.availablePorts);
		Integer port = (Integer) this.availablePorts.get(0);
		this.portsInUse.add(port);
		this.availablePorts.remove(port);
		return port;
	}

	public synchronized void release(Integer port) {
		this.portsInUse.remove(port);
		if ((port.intValue() >= this.minPort.intValue())
				&& (port.intValue() <= this.maxPort.intValue())
				&& (isEvenNumber(port)))
			this.availablePorts.add(port);
	}

	private boolean isEvenNumber(Integer port) {
		if (port == null) {
			return false;
		}
		return (port.intValue() % 2 == 0);
	}

}
