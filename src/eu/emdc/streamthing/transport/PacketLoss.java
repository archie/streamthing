package eu.emdc.streamthing.transport;

import eu.emdc.streamthing.DelayTuple;
import eu.emdc.streamthing.NodeConfig;
import eu.emdc.streamthing.StreamThing;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.transport.Transport;

public class PacketLoss implements Transport {

	/* configuration keywords */
	private static final String PAR_TRANSPORT = "transport";
	private static final String NODE_LATENCY = ".latencyfile";
	
	private final int transport;
	private NodeConfig m_nodeConfig; 
	
	public PacketLoss(String prefix) {
		transport = Configuration.getPid(prefix+"."+PAR_TRANSPORT);
		// Read node config
		m_nodeConfig = new NodeConfig();
		m_nodeConfig.InitialiseLatencyMap(Configuration.getString(prefix + NODE_LATENCY));
		
	}
	
	
	public long getLatency(Node src, Node dest) {
		System.out.println("Delay between " + src.getID() + " and " + dest.getID());
		/*DelayTuple dt = null;
		if ((dt = m_nodeConfig.GetDelayTupleForNodePair(StreamThing.GetStreamIdFromNodeId(src.getID()), 
				StreamThing.GetStreamIdFromNodeId(dest.getID()))) != null) {
			System.out.println("Delay: " + dt.GetMinDelay() + " - " + dt.GetMaxDelay());
			return CommonState.r.nextLong() - (long)dt.GetMinDelay();
		}*/
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
