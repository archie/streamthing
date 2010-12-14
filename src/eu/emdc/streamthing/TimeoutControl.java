package eu.emdc.streamthing;

import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.edsim.EDSimulator;

public class TimeoutControl implements Control {

	private static final String PAR_PROTOCOL = "protocol";
	private int streamThingProtocol; 
	
	public TimeoutControl(String prefix) {
		streamThingProtocol = Configuration.getPid(prefix + "." + PAR_PROTOCOL);
	}
	
	public boolean execute() {
		StreamEvent event = new StreamEvent();
		event.SetEventType(StreamEventType.TIMEOUT);
		for (int i = 0; i < Network.size(); i++) {
			if (Network.get(i).isUp()) {
				EDSimulator.add(0, event, Network.get(i), streamThingProtocol);
			}
		}
		return false;
	}
}
