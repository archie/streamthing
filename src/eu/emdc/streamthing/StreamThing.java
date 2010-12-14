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
	protected NodeWorld m_world;
	public boolean hasJoined = false;
	public int m_myStreamNodeId;
	
	static public Map<Integer, Long> m_streamIdToNodeId = new HashMap<Integer, Long>();

	static public Map<Integer, Integer> m_videoStreamToStreamNodeId = new HashMap<Integer, Integer>();

	
	static public int GetStreamIdFromNodeId(long nodeId) {
		Iterator<Entry<Integer, Long>> iter = m_streamIdToNodeId.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Integer, Long> entry = iter.next();
			if (entry.getValue() == nodeId)
				return entry.getKey();
		}
			
		return -1;
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

	@Override
	public Object clone() {
		StreamThing s = null;
		try {
			s = (StreamThing) super.clone();
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
			m_streamIdToNodeId.put (m_myStreamNodeId,  src.getID());
			
			break;
		case LEAVE:
			// inform children/parent
			// (remove from required trees)
			//
			
			//System.out.println("I actually enter this place");
			m_streamIdToNodeId.remove(m_myStreamNodeId);
			break;
		case PUBLISH:
			// Add to video stream to streamNodeId map
			// Add to multicast tree
			m_videoStreamToStreamNodeId.put (msg.GetEventParams().get(0).intValue(), m_myStreamNodeId);	
			
			// I am now the root of a multicast tree;
			break;
		case SUBSCRIBE:
			
			/* send subscription message */
			
			break;
		case UNSUBSCRIBE:
			// do unsubscribe
			break;
		default:
			break;
		}


	}
	
}
