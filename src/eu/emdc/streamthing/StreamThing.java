package eu.emdc.streamthing;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import eu.emdc.streamthing.message.*;
import eu.emdc.streamthing.stats.Debug;

import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.pastry.MSPastryCommonConfig;
import peersim.pastry.MSPastryProtocol;
import peersim.transport.Transport;

public class StreamThing extends MSPastryProtocol implements CDProtocol, EDProtocol {

	/* configuration keywords */
	private static final String NODE_LATENCY = ".latencyfile";
	private static final String NODE_CAPACITY = ".capacityfile";
	
	/* implementation */
	protected String prefix;
	private StreamManager m_streamManager;
	private NodeWorld m_world;
	private VideoCreator m_creator;

	public StreamThing(String prefix) {
		super (prefix);
		this.prefix = prefix;
		
		// Read node config
		NodeConfig nodeConf = new NodeConfig();
		nodeConf.InitialiseLatencyMap(Configuration.getString(prefix + NODE_LATENCY));
		nodeConf.InitialiseUploadCapacity(Configuration.getString(prefix + NODE_CAPACITY));
		
		// StreamThing helpers
		m_world = new NodeWorld();
		m_streamManager = new StreamManager(m_world);
		
	}

	@Override
	public void nextCycle(Node node, int protocolID) {
		// where running event driven
	}

	@Override
	public void processEvent(Node node, int pid, Object event) {
		
		if (event instanceof peersim.pastry.Message)
		{
			super.processEvent(node, pid, event);
		}
		else 
		{
			if (event instanceof StreamEvent) {
				handleTrigger(node, (StreamEvent) event, pid);
				return;
			}
			else if (event instanceof VideoMessage) {
				m_streamManager.processVideoMessage(node, (VideoMessage) event);
			}
			else if (event instanceof StreamMessage) {
				m_creator.streamVideo(node, pid);
			}
			else if (event instanceof Message) {
				handleMessage(node, (Message) event, pid);
				return;
			}
			else {
				System.err.println("Unknown message!");
			}
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
		Debug.info("Node: " + node.getID() + " got " + join.toString());
	}

	private void partMsg(Node node, int pid, Message part) {
		Debug.info(part.toString());
	}

	/** 
	 * Handle simulation events based on those specified in input file
	 */
	private void handleTrigger(Node src, StreamEvent msg, int pid) {
		Transport transport = (Transport) src.getProtocol(FastConfig
				.getTransport(pid));
		Debug.info("Parsing msg: " + msg.toString());
		switch (msg.GetEventType()) {
		case JOIN:
			// ask random node to join (should be closest node... right?)
			Node dest = Network.get(CommonState.r.nextInt(Network.size()));
			transport.send(src, dest, new Message(MessageType.JOIN, src), pid);
			join();
			break;
		case LEAVE:
			// notify my friends I'm leaving
			break;
		case PUBLISH:
			// hash stream id
			// locate resp node
			// send store ref to node
			
			if (m_creator == null) {
				m_creator = new VideoCreator(m_world, transport, msg);
				m_creator.scheduleStream(src, pid);
				Debug.info(src.getID() + " published a new stream");
			}
			break;
		case SUBSCRIBE:
			// lookup(hash(stream_id))
			/*peersim.pastry.Message lookup = peersim.pastry.Message.makeLookUp(1);
			
			Message subscribeMsg = new Message(MessageType.SUBSCRIBE, src);
			transport.send(src, dest, subscribeMsg, pid);*/
			break;
		case UNSUBSCRIBE:
			// do unsubscribe
			break;
		default:
			break;
		}

		
	}
}
