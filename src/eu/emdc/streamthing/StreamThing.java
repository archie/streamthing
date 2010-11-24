package eu.emdc.streamthing;

import java.util.List;
import java.util.Map;

import eu.emdc.streamthing.message.*;


import peersim.cdsim.CDProtocol;
import peersim.core.Node;
import peersim.edsim.EDProtocol;

public class StreamThing implements CDProtocol, EDProtocol {

	/* configuration keywords */
	private static final String NODE_LATENCY = "node.latency";
	private static final String NODE_CAPACITY = "node.capacity";
		
	/* implementation */
	protected String prefix;
	
	private Map<Node, NodeConfig> routingTable;
	private boolean loadConfig = true;
	
	public StreamThing(String prefix) {
		this.prefix = prefix;
		//routingTable = NodeConfiguration.load(NODE_LATENCY, NODE_CAPACITY);
		NodeConfig nodeConf = new NodeConfig();
		nodeConf.InitialiseLatencyMap("filelolz");
		nodeConf.InitialiseUploadCapacity("uploadFile");
		
		DelayTuple temp = new DelayTuple();
		temp = nodeConf.GetDelayTupleForNodePair(2, 2);
		System.out.println(temp.GetMinDelay () + " " + temp.GetMaxDelay());
	}
	
	@Override
	public void nextCycle(Node node, int protocolID) {
		if (loadConfig) {
			System.out.println("Loading node configuration");
			loadConfig = false;
		}
	}

	@Override
	public void processEvent(Node node, int pid, Object event) {
		if (event != null) {
			Message msg = (Message) event;
			switch (msg.type) {
			case JOIN:
				join(node, pid, msg);
				break;
			case PART:
				part(node, pid, msg);
				break;
			default:
				break;
			}
		} else {
			// ignore for now
		}
	}
	
	@Override
	public Object clone() {
		return this;
	}
	
	/* private methods */
	private void join(Node node, int pid, Message join) {
		System.out.println(join.toString());
	}
	
	private void part(Node node, int pid, Message part) {
		System.out.println("Node leaves");
	}
}

