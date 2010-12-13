package eu.emdc.streamthing;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import peersim.pastry.UniformRandomGenerator;
import peersim.transport.Transport;

public class StreamThing implements Cloneable, CDProtocol, EDProtocol {

	private static final String PAR_CAPACITY = "capacityfile";
	public NodeConfig m_nodeConfig = new NodeConfig();
	
	/* implementation */
	protected String prefix;
	protected StreamManager m_streamManager;
	protected NodeWorld m_world;
	protected MSPastryProtocol m_pastry;
	public int m_myStreamNodeId;
	
	static public Map< Integer, BigInteger> HashFunction = new HashMap<Integer, BigInteger>(); 
	protected List<Integer> m_mySubscriptions = new ArrayList<Integer>();
	protected Map<Integer, Integer> m_streamToPublisherStreamNodeId = new HashMap<Integer, Integer>();
	protected List<Integer> m_StreamsISubscribeTo = new ArrayList<Integer>();
	
	static public long GetNodeIdFromStreamId (int streamId){
		for (int i = 0; i < Network.size(); i++)
		{
			StreamThing s = (StreamThing) Network.get(i).getProtocol(Configuration.lookupPid("streamthing"));
			
			if (s.m_myStreamNodeId == streamId)
			{
				return Network.get(i).getID();
			}
		}
		
		return -1;
	}
	
	static public Node GetNodeFromNodeId(long nodeId) {
		for (int i = 0; i < Network.size(); i++)
		{
			if (Network.get(i).getID() == nodeId)
				return Network.get(i);
		}
		return null;
	}
	
	static public int GetStreamIdFromNodeId(long nodeId) {
		for (int i = 0; i < Network.size(); i++) {
			if (Network.get(i).getID() == nodeId)	 {	
				StreamThing s = (StreamThing) Network.get(i).getProtocol(Configuration.lookupPid("streamthing"));
				return s.m_myStreamNodeId;
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
		m_nodeConfig.InitialiseUploadCapacity(Configuration.getString(prefix + "." + PAR_CAPACITY));
		// StreamThing helpers
		m_world = new NodeWorld();
		
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
				m_streamManager.streamVideo(node, pid);
			}
			else if (event instanceof VideoTransportEvent) {
				m_streamManager.transportVideoMessages(node, pid);
			}
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
			s.m_world = new NodeWorld();
			s.m_streamManager = null;
			s.m_nodeConfig = new NodeConfig();
			s.m_myStreamNodeId = -1;
			
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
        return s;
	}

	/* protocol methods */
	private void handleMessage(Node src, StreamMessage msg, int pid) {
		switch (msg.type) {
		case SUBSCRIBE_ACK:
			m_StreamsISubscribeTo.add(msg.streamId);
			System.out.println(m_myStreamNodeId + " got a subscribe ack message from " + msg.source + " on stream id " + msg.streamId
					+ " publisher is " + msg.publisher);
			break;
		}
	}

	/** 
	 * Handle simulation events based on those specified in input file
	 */
	private void handleTrigger(Node src, StreamEvent msg, int pid) {
		Transport transport = (Transport) src.getProtocol(FastConfig
				.getTransport(pid));
		//Debug.info(src.getID() + "Parsing msg: " + msg.toString());
				
		switch (msg.GetEventType()) {
		case JOIN:
			m_myStreamNodeId = msg.GetNodeId();
			//System.out.println();
			m_pastry = (MSPastryProtocol) src.getProtocol(Configuration.lookupPid("3mspastry"));
			
			m_pastry.setListener(new MSPastryProtocol .Listener() {
				
				@Override
				public void receive(Message m) {
					//handleMessage(m.src, m.dest, (StreamMessage)m.body);
					// TODO Auto-generated method stub
					Message data = (Message)m.body;
					if (data.body instanceof StreamMessage) {
						StreamMessage innerMessage = (StreamMessage)data.body;
						
						switch (innerMessage.type) {
						case PUBLISH: 
							m_streamToPublisherStreamNodeId.put(innerMessage.streamId, innerMessage.source);
							System.out.println(m_myStreamNodeId + " received a publish from " + innerMessage.source + " to stream " + innerMessage.streamId);
							break;
						case SUBSCRIBE:
							if (m_streamToPublisherStreamNodeId.containsKey(innerMessage.streamId)) {
								m_mySubscriptions.add(innerMessage.source);
								
								/* send acknowledge message outside pastry */
								StreamMessage replyMessage = new StreamMessage(MessageType.SUBSCRIBE_ACK);
								replyMessage.source = m_myStreamNodeId;
								replyMessage.streamId = innerMessage.streamId;
								replyMessage.publisher = m_streamToPublisherStreamNodeId.get(innerMessage.streamId);
								
								// peersim hack
								int mypid = Configuration.lookupPid("streamthing");
								Transport myTransport = (Transport) GetNodeFromNodeId(GetNodeIdFromStreamId(m_myStreamNodeId)).getProtocol(FastConfig
										.getTransport(mypid));
								
								myTransport.send(GetNodeFromNodeId(GetNodeIdFromStreamId(m_myStreamNodeId)), 
										GetNodeFromNodeId(GetNodeIdFromStreamId(innerMessage.source)), replyMessage, mypid);
								//m_pastry.send(replyMessage.dest, replyMessage);
								
								System.out.println(m_myStreamNodeId + " received a subscribe from " + innerMessage.source + "to stream" + innerMessage.streamId);
							} else {
								System.out.println("severe error in input file");
							}
							break;
						}
						
					//String d = (String) ((Message) data).body;
						System.out.println("received message < " +
							m.dest + " " + innerMessage.toString() +
							" > from address: "+ m.src);
					}
				}
			});
			
			m_pastry.join();
			
			break;
		case LEAVE:
			//System.out.println("I actually enter this place");
			;
			break;
		case PUBLISH:
			
			if (m_streamManager == null) {
				Float capacity = m_nodeConfig.GetUploadCapacityForNode(msg.GetNodeId());
				if (capacity == null) 
					capacity = new Float(0);
				m_streamManager = new StreamManager(m_world, transport, msg, capacity.intValue());
				/*m_streamManager.scheduleStream(src, pid);*/
				Debug.info(src.getID() + " published a new stream");
			}
					
			/* message so that subscribers can find the stream in the network */
			StreamMessage streamMessage = new StreamMessage(MessageType.PUBLISH);
			streamMessage.streamId = msg.GetEventParams().get(0).intValue();
			streamMessage.source = msg.GetNodeId();
			
			Message createMsg = new Message(streamMessage);
			UniformRandomGenerator urg = new UniformRandomGenerator(
                    MSPastryCommonConfig.BITS, CommonState.r);
			BigInteger temp = urg.generate();
			createMsg.dest = temp; 
			
			HashFunction.put(msg.GetEventParams().get(0).intValue(), temp);
			
			System.out.println(temp);
			
			m_pastry.send(temp, createMsg);
			
			break;
		case SUBSCRIBE:
			
			/* send subscription message */
			StreamMessage sm = new StreamMessage(MessageType.SUBSCRIBE);
			sm.streamId = msg.GetEventParams().get(0).intValue();
			sm.source = msg.GetNodeId();
			
			Message subscribeMsg = new Message(sm);
			subscribeMsg.dest = HashFunction.get(msg.GetEventParams().get(0).intValue());
			m_pastry.send(subscribeMsg.dest, subscribeMsg);

			break;
		case UNSUBSCRIBE:
			// do unsubscribe
			break;
		default:
			break;
		}


//		for (int i = 0; i < Network.size(); i++)	
//		{
//			Node n = Network.get(i);
//			MSPastryProtocol p = (MSPastryProtocol) n.getProtocol(Configuration.lookupPid("3mspastry"));
//			StreamThing s = (StreamThing) n.getProtocol(Configuration.lookupPid("streamthing"));
//			
//			System.out.println(n.getID() + " " + s.m_streamId + " " + p.nodeId);
//		}
	}
	
	public boolean isSubscribingTo(int streamId) {
		return m_StreamsISubscribeTo.contains(streamId);
	}
}
