package eu.emdc.streamthing.transport;

import peersim.config.Configuration;
import peersim.core.Node;
import peersim.transport.Transport;

public class PacketLoss implements Transport {

	private static final String PAR_TRANSPORT = "transport";
	private final int transport;
	
	public PacketLoss(String prefix) {
		transport = Configuration.getPid(prefix+"."+PAR_TRANSPORT);
	}
	
	@Override
	public long getLatency(Node src, Node dest) {
		// pass on for now
		return 0;
	}

	@Override
	public void send(Node src, Node dest, Object msg, int pid) {
		// pass on for now
		Transport t = (Transport) src.getProtocol(transport);
		t.send(src, dest, msg, pid);
	}
	
	@Override 
	public Object clone() {
		return this;
	}

}
