package eu.emdc.streamthing;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import peersim.transport.Transport;

public class StreamThing implements Cloneable, CDProtocol, EDProtocol {

	private static final String PAR_CAPACITY = "capacityfile";
	public NodeConfig m_nodeConfig = new NodeConfig();
	
	/* implementation */
	protected String prefix;
	protected StreamManager m_streamManager;
	public boolean hasJoined = false;
	public int m_myStreamNodeId;
	protected List<Integer> m_streamsISubscribeTo; 
	
	static public Map<Integer, Long> m_streamIdToNodeId = new HashMap<Integer, Long>();

	static public Map<Integer, Integer> m_videoStreamToStreamNodeId = new HashMap<Integer, Integer>();
	
	static public Map<Integer, NodeWorld> m_videoStreamIdToMulticastTreeMap = new HashMap<Integer, NodeWorld>();

	
	static public int GetStreamIdFromNodeId(long nodeId) {
		Iterator<Entry<Integer, Long>> iter = m_streamIdToNodeId.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Integer, Long> entry = iter.next();
			if (entry.getValue() == nodeId)
				return entry.getKey();
		}
			
		return -1;
	}
	
	static public Node GetNodeFromNodeId(long nodeId) {
		for (int i = 0; i < Network.size(); i++) {
			if (Network.get(i).getID() == nodeId)
				return Network.get(i);
		}
		return null;
	}
	
	public StreamThing(String prefix) {
		this.prefix = prefix;
		m_nodeConfig.InitialiseUploadCapacity(Configuration.getString(prefix + "." + PAR_CAPACITY));
		m_streamsISubscribeTo = new ArrayList<Integer>();
		// StreamThing helpers
		
	}

	@Override
	public void nextCycle(Node node, int protocolID) {
		// where running event driven
	}

	@Override
	public void processEvent(Node node, int pid, Object event) {
		
			if (event instanceof StreamEvent) {
				handleTrigger(node, (StreamEvent) event, pid);
				return;
			}
			else if (event instanceof VideoMessage) {
				VideoMessage eventMsg = (VideoMessage) event;
				if (m_streamsISubscribeTo.contains(eventMsg.streamId))
					m_streamManager.processVideoMessage(node, eventMsg, pid);
			}
			else if (event instanceof VideoPublishEvent) {
				System.out.println("publish event by " + node.getID() + " sid " + GetStreamIdFromNodeId(node.getID()));
				m_streamManager.streamVideo(node, (VideoPublishEvent) event, pid);
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

	@Override
	public Object clone() {
		StreamThing s = null;
		try {
			s = (StreamThing) super.clone();
			s.m_streamManager = null;
			s.m_nodeConfig = new NodeConfig();
			s.m_myStreamNodeId = -1;
			s.m_streamsISubscribeTo = new ArrayList<Integer>();
			
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
        return s;
	}

	/* protocol methods */
	private void handleMessage(Node src, StreamMessage msg, int pid) {
		switch (msg.type) {
		case SUBSCRIBE_ACK:
		
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
			// update stream ID
			// update global map of stream node id to node id
			m_myStreamNodeId = msg.GetNodeId();
			StreamThing.m_streamIdToNodeId.put (m_myStreamNodeId,  src.getID());
			
			
			break;
		case LEAVE:
			// inform children/parent
			// (remove from required trees)
			//
			
			//System.out.println("I actually enter this place");
			//m_streamIdToNodeId.remove(m_myStreamNodeId);
			break;
		case PUBLISH:
			// Add to video stream to streamNodeId map
			m_videoStreamToStreamNodeId.put (msg.GetEventParams().get(0).intValue(), m_myStreamNodeId);	
			
			// Create new multicast tree
			System.out.println(m_myStreamNodeId + " is going to publish ");
			Float f = m_nodeConfig.GetUploadCapacityForNode(m_myStreamNodeId);
			if (f == null) {
				f = new Float(5000);
			}
			NodeWorld nw = new NodeWorld (msg.GetEventParams().get (0).intValue(), m_myStreamNodeId, f.intValue());
			m_videoStreamIdToMulticastTreeMap.put (msg.GetEventParams().get (0).intValue(), nw);
			
			// I am now the root of a multicast tree;
			
			// start streaming
			if (m_streamManager == null) {
				m_streamManager = new StreamManager(transport, f.intValue());
			}
			m_streamManager.publishNewStream(msg);
			m_streamManager.scheduleStream(src, pid, msg.GetEventParams().get(0).intValue());
			
			break;
		case SUBSCRIBE:
			// Join the multicast tree
			
			NodeWorld nwToJoin = m_videoStreamIdToMulticastTreeMap.get (msg.GetEventParams().get(0).intValue());
			
			System.out.println(m_myStreamNodeId +" Imma subscribe to : " + msg.GetEventParams().get(0));
			nwToJoin.AddNode(m_myStreamNodeId, m_nodeConfig.GetUploadCapacityForNode(m_myStreamNodeId));

			Float fu = m_nodeConfig.GetUploadCapacityForNode(m_myStreamNodeId);
			if (fu == null) {
				fu= new Float(5000);
			}
			
			// in case I'm not a publisher (most likely), I need a stream manager to be able to forward data
			if (m_streamManager == null) {
				m_streamManager = new StreamManager(transport, fu.intValue());
			}
			m_streamsISubscribeTo.add(msg.GetEventParams().get(0).intValue());
			
			
			break;
		case UNSUBSCRIBE:
			// do unsubscribe
			
			// Remove from multicast tree
			
			// Notify StreamManager
			break;
		default:
			break;
		}


	}
	
}
