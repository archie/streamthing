package eu.emdc.streamthing;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import peersim.core.Node;
import peersim.edsim.EDSimulator;
import peersim.transport.Transport;

import eu.emdc.streamthing.message.VideoPublishEvent;
import eu.emdc.streamthing.message.VideoMessage;

public class StreamManager {
	private StreamState m_currentState = StreamState.IDLE;
	private Queue<VideoMessage> m_buffer; // needs to be made global
	
	private Map<Integer, StreamData> m_streams = new HashMap<Integer, StreamData>();
	private Transport m_transport;
	private Queue<VideoMessage> m_output;
	private int m_queuesize;
	
	class StreamData {
		public int duration;
		public int rate;
		public StreamData(int duration, int rate) {
			this.duration = duration;
			this.rate = rate;
		}
	}
	
	public StreamManager(Transport transport, int uploadCapacity) {
		m_transport = transport;
		m_output = new LinkedList<VideoMessage>();

		m_queuesize = 5000;
		m_buffer = new LinkedList<VideoMessage>(); // FIFO
	}
	
	public void publishNewStream(StreamEvent pubEvent) {
		// parse pubEvent
		m_streams.put(pubEvent.GetEventParams().get(0).intValue(),
				new StreamData(pubEvent.GetEventParams().get(1).intValue(), 
						pubEvent.GetEventParams().get(2).intValue()));
		
	}
	
	public void scheduleStream(Node src, int pid, int streamId) {
		int streamDuration = m_streams.get(streamId).duration;
		VideoPublishEvent pve = new VideoPublishEvent();
		pve.streamId = streamId;
		for (int i = 0; i < streamDuration; i++) {
			EDSimulator.add(i, pve, src, pid);
		}
	}

	
	public void streamVideo(Node src, VideoPublishEvent event, int pid) {
		
		/*
		 *  rate control 
		 *   if stream rate is 1000
		 *   and two nodes are subscribing
		 *   and your upload capacity is 1000
		 *   
		 *   How to divide the up capacity equal?
		 *    - round robin? (equal drop for all nodes)
		 *    - all to one, push subscribing node (causing overload) down? 
		 *   
		 */
		int streamNodeId = StreamThing.GetStreamIdFromNodeId(src.getID());
		//System.out.println("Stream Node ID " + streamNodeId + " streaming video with stream " + event.streamId );
		
		List<Integer> children = StreamThing.m_videoStreamIdToMulticastTreeMap.get(event.streamId).GetChildren(streamNodeId);
		
		// in this implementation using the example above, the second node will not get any packets.
		VideoMessage streamMsg;
		
		int outRate = 5;
		
		for (int i = 0; i < outRate; i++) {	
				for (int dest : children) {
					streamMsg = new VideoMessage(src);
					streamMsg.streamId = event.streamId;
					streamMsg.destStreamNodeId= dest;
				
				//if (m_output.size() <= m_queuesize) {
					m_output.add(streamMsg);
				//} else {
				//	System.out.println("dropped a packet");
				//}
				}
				EDSimulator.add(outRate+i, new VideoTransportEvent(), src, pid);			
		}
		
		
	}

	public void transportVideoMessages(Node src, int pid) {
		VideoMessage msg;
		while ((msg = m_output.poll()) != null) {
			m_transport.send(src, StreamThing.GetNodeFromNodeId(StreamThing.m_streamIdToNodeId.get(msg.destStreamNodeId)), msg, pid);
		}
	}
	
	public void processVideoMessage(Node node, VideoMessage msg, int pid) {
		// should I forward?
		
		m_buffer.add(msg);
		int streamNodeId = StreamThing.GetStreamIdFromNodeId(node.getID());
		List<Integer> children = StreamThing.m_videoStreamIdToMulticastTreeMap.get(msg.streamId).GetChildren(streamNodeId);
		
		if (children.size() == 0) { 
			consumeVideo(node, msg.streamId);
		} else {
			//System.out.println("Stream Node ID " + streamNodeId + " forwarding stream " + msg.streamId );
			
			forwardData(streamNodeId, node, msg, children, pid);
			consumeVideo(node, msg.streamId);
		}
		
	}
	
	private void forwardData(int streamNodeId, Node node, VideoMessage messageToForward, List<Integer> children, int pid) {
		VideoMessage streamMsg = null; 
		int outRate = 5;
		
		for (int i = 0; i < outRate; i++) {	
				for (int dest : children) {
					streamMsg = new VideoMessage(node);
					streamMsg.streamId = messageToForward.streamId;
					streamMsg.destStreamNodeId= dest;
				
				//if (m_output.size() <= m_queuesize) {
					m_output.add(streamMsg);
				//} else {
				//	System.out.println("dropped a packet");
				//}
				}
				EDSimulator.add(outRate+i, new VideoTransportEvent(), node, pid);			
		}
	}
	
	private void consumeVideo(Node node, int streamId) {
		for (VideoMessage msg : m_buffer) {
			//System.out.println(StreamThing.GetStreamIdFromNodeId(node.getID()) + " consuming video msg: " + msg.id);
		}
		m_buffer.clear();
	}
}
