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
	public int m_streamId;
	
	static public long GetNodeIdFromStreamId (int streamId){
		for (int i = 0; i < Network.size(); i++)
		{
			StreamThing s = (StreamThing) Network.get(i).getProtocol(Configuration.lookupPid("streamthing"));
			
			if (s.m_streamId == streamId)
			{
				return Network.get(i).getID();
			}
		}
		
		return -1;
	}
	
	static public BigInteger GetPastryIdFromNodeId (long nodeid){
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
			else if (event instanceof PublishVideoEvent) {
				m_creator.streamVideo(node, pid);
			}
			else if (event instanceof StreamMessage) {
				//handleMessage(node, (StreamMessage) event, pid);
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
	private void handleMessage(BigInteger src, BigInteger dest, StreamMessage msg) {
		switch (msg.type) {
		case PUBLISH:
			
			break;
		default:
			break;
		}
	}

	/** 
	 * Handle simulation events based on those specified in input file
	 */
	private void handleTrigger(Node src, StreamEvent msg, int pid) {
		Transport transport = (Transport) src.getProtocol(FastConfig
				.getTransport(pid));
		Debug.info(src.getID() + "Parsing msg: " + msg.toString());
				
		switch (msg.GetEventType()) {
		case JOIN:
			m_streamId = msg.GetNodeId();
			//System.out.println();
			m_pastry = (MSPastryProtocol) src.getProtocol(Configuration.lookupPid("3mspastry"));
			m_pastry.setListener(new MSPastryProtocol .Listener() {
				
				@Override
				public void receive(Message m) {
					handleMessage(m.src, m.dest, (StreamMessage)m.body);
					// TODO Auto-generated method stub
					Object data = m.body;
					System.out.println("received message < " +
							m.dest +
							" > from address: "+ m.src);
					
				}
			});
			m_pastry.join();
			
			break;
		case LEAVE:
			System.out.println("I actually enter this place");
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
			
			if (GetNodeIdFromStreamId(msg.GetEventParams().get(0).intValue()) >= 0) 
			{
				m_pastry.send(GetPastryIdFromNodeId(GetNodeIdFromStreamId(msg.GetEventParams().get(0).intValue())), subscribeMsg);
			}
			break;
		case UNSUBSCRIBE:
			// do unsubscribe
			break;
		default:
			break;
		}


		for (int i = 0; i < Network.size(); i++)	
		{
			Node n = Network.get(i);
			MSPastryProtocol p = (MSPastryProtocol) n.getProtocol(Configuration.lookupPid("3mspastry"));
			StreamThing s = (StreamThing) n.getProtocol(Configuration.lookupPid("streamthing"));
			
			System.out.println(n.getID() + " " + s.m_streamId + " " + p.nodeId);
		}
	}
}
