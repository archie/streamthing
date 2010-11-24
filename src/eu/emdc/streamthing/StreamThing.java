package eu.emdc.streamthing;

import java.util.List;
import java.util.Map;

import eu.emdc.streamthing.message.*;

import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;

public class StreamThing implements CDProtocol, EDProtocol {

	/* configuration keywords */
	private static final String NODE_LATENCY = ".latencyfile";
	private static final String NODE_CAPACITY = ".capacityfile";
	
	/* implementation */
	protected String prefix;

	public StreamThing(String prefix) {
		this.prefix = prefix;
		NodeConfig nodeConf = new NodeConfig();
		nodeConf.InitialiseLatencyMap(Configuration.getString(prefix + NODE_LATENCY));
		nodeConf.InitialiseUploadCapacity(Configuration.getString(prefix + NODE_CAPACITY));
		
	}

	@Override
	public void nextCycle(Node node, int protocolID) {
		// where running event driven
	}

	@Override
	public void processEvent(Node node, int pid, Object event) {
		if (event != null) {
			if (event instanceof StreamEvent) {
				handleTrigger(node, (StreamEvent) event, pid);
				return;
			}
			if (event instanceof Message) {
				handleMessage(node, (Message) event, pid);
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
		/* delegate to protocol message handler */ 
		switch (msg.type) {
		case JOIN:
			joinMsg(node, pid, msg);
			break;
		case PART:
			partMsg(node, pid, msg);
			break;
		default:
			break;
		}
	}

	private void joinMsg(Node node, int pid, Message join) {
		System.out.println(join.toString());
	}

	private void partMsg(Node node, int pid, Message part) {
		System.out.println("Node leaves");
	}

	/* trigger methods */
	private void handleTrigger(Node node, StreamEvent msg, int pid) {
		/* delegate to simulation event handler */
		Transport transport = (Transport) node.getProtocol(FastConfig
				.getTransport(pid));
		
		switch (msg.GetEventType()) {
		case JOIN:
			// ask random node to join (should be closest node... right?)
			Node dest = Network.get(CommonState.r.nextInt(Network.size()));
			transport.send(node, dest, new Message(MessageType.JOIN, node), pid);
			break;
		case LEAVE:
			// notify my friends I'm leaving
			break;
		case PUBLISH:
			// do publish
			break;
		case SUBSCRIBE:
			// do subscribe
			break;
		case UNSUBSCRIBE:
			// do unsubscribe
			break;
		default:
			break;
		}

	}
}
