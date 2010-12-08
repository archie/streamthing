package eu.emdc.streamthing.transport;

import eu.emdc.streamthing.NodeConfig;
import peersim.config.Configuration;
import peersim.core.Node;
import peersim.transport.Transport;

public class PacketLoss implements Transport {

	/* configuration keywords */
	private static final String PAR_TRANSPORT = "transport";
	private static final String NODE_LATENCY = ".latencyfile";
	private static final String NODE_CAPACITY = ".capacityfile";
	
	private final int transport;
	
	public PacketLoss(String prefix) {
		transport = Configuration.getPid(prefix+"."+PAR_TRANSPORT);
		// Read node config
		NodeConfig nodeConf = new NodeConfig();
		nodeConf.InitialiseLatencyMap(Configuration.getString(prefix + NODE_LATENCY));
		nodeConf.InitialiseUploadCapacity(Configuration.getString(prefix + NODE_CAPACITY));
		
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
