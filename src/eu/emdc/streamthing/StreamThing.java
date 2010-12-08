package eu.emdc.streamthing;

import java.math.BigInteger;

import eu.emdc.streamthing.message.*;
import eu.emdc.streamthing.stats.Debug;

import peersim.cdsim.CDProtocol;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.IdleProtocol;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.pastry.MSPastryCommonConfig;
import peersim.pastry.MSPastryProtocol;
import peersim.pastry.Message;
import peersim.transport.Transport;

public class StreamThing implements Cloneable, CDProtocol, EDProtocol {

	/* configuration keywords */
	private static final String NODE_LATENCY = ".latencyfile";
	private static final String NODE_CAPACITY = ".capacityfile";
	
	/* implementation */
	protected String prefix;
	protected StreamManager m_streamManager;
	protected NodeWorld m_world;
	protected VideoCreator m_creator;
	protected MSPastryProtocol m_pastry;
	
	static public BigInteger GetPastryIdFromNodeId (int nodeid){
		for (int i = 0; i < Network.size(); i++)
		{
			if (Network.get(i).getID() == nodeid)
			{
				MSPastryProtocol p = (MSPastryProtocol) Network.get(i).getProtocol(Configuration.lookupPid("3mspastry"));
				return p.nodeId;
			}
		}
		
		return null;
	} 
	
	public StreamThing(String prefix) {
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
			// lolz
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
			//else if (event instanceof StreamMessage) {
//				m_creator.streamVideo(node, pid);
			//}
			else if (event instanceof StreamMessage) {
				handleMessage(node, (StreamMessage) event, pid);
				return;
			}
			else {
				System.err.println("Unknown message!");
			}
		}
	}

	@Override
	public Object clone() {
		StreamThing s = null;
		try {
			s = (StreamThing) super.clone();
			s.m_pastry = null;
			s.m_creator = null;
			s.m_world = new NodeWorld();
			s.m_streamManager = new StreamManager(s.m_world);
			
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
        return s;
	}

	/* protocol methods */
	private void handleMessage(Node node, StreamMessage msg, int pid) {
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

	private void joinMsg(Node node, int pid, StreamMessage msg) {
		Debug.info("Node: " + node.getID() + " got " + msg.toString());
	}

	private void partMsg(Node node, int pid, StreamMessage part) {
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

			//System.out.println();
			m_pastry = (MSPastryProtocol) src.getProtocol(Configuration.lookupPid("3mspastry"));
			m_pastry.setListener(new MSPastryProtocol .Listener() {
				
				@Override
				public void receive(Message m) {
					// TODO Auto-generated method stub
					Object data = m.body;
					System.out.println("received message < " +
							data.toString() +
							" > from address: "+ m.src);
					
				}
			});
			m_pastry.join();
			break;
		case LEAVE:
			;
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
			peersim.pastry.Message lookup = peersim.pastry.Message.makeLookUp(1);
			
			Message subscribeMsg = new Message("zzz");
			m_pastry.send(GetPastryIdFromNodeId(msg.GetEventParams().get(0).intValue()), subscribeMsg);
			//transport.send(src, dest, subscribeMsg, pid);
			break;
		case UNSUBSCRIBE:
			// do unsubscribe
			break;
		default:
			break;
		}

		
	}
}
