package eu.emdc.streamthing;

import java.util.List;
import java.util.Map;

import eu.emdc.streamthing.message.*;

import peersim.cdsim.CDProtocol;
import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;

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
			if (event instanceof StreamEvent) {
				handleTrigger(node, (StreamEvent) event, pid);
				return;
			}
			if (event instanceof Message) {
				handleMessage(node, (Message)event, pid);
				return; 
			}
		} else {
			// naaeh
		}
	}

	@Override
	public Object clone() {
		return this;
	}

	/* protocol methods */
	private void handleMessage(Node node, Message msg, int pid) {
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
	}
	private void join(Node node, int pid, Message join) {
		System.out.println(join.toString());
	}

	private void part(Node node, int pid, Message part) {
		System.out.println("Node leaves");
	}
	
	/* trigger methods */
	private void handleTrigger(Node node, StreamEvent msg, int pid) {
		Transport transport = (Transport) node.getProtocol(FastConfig
				.getTransport(pid));
		if (msg.type == StreamEventType.JOIN) {
			// ask random node to join
			Node dest = Network.get(CommonState.r.nextInt(Network.size()));
			transport.send(node, dest, new Message(MessageType.JOIN, node), pid);
		} else {
			// else ?? 
		}
		
	}
}
