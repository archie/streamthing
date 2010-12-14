package eu.emdc.streamthing;

import java.util.HashMap;
import java.util.LinkedList;
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
	private NodeWorld m_world;
	private Transport m_transport;
	private Queue<VideoMessage> m_output;
	private int m_uploadCapacity;
	private int m_queuesize;
	
	class StreamData {
		public int duration;
		public int rate;
		public StreamData(int duration, int rate) {
			this.duration = duration;
			this.rate = rate;
		}
	}
	
	public StreamManager(NodeWorld world, Transport transport, int uploadCapacity) {
		m_world = world;
		m_transport = transport;
		m_output = new LinkedList<VideoMessage>();
		m_uploadCapacity = uploadCapacity;

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
		 *    - round robin?
		 *    - all to one, push subscribing down? 
		 */
		
		
		// in this implementation using the example above, the second node will not get any packets.
		VideoMessage streamMsg;
		
		int streamRate = m_streams.get(event.streamId).rate;
		
		for (int i = 0; i < streamRate; i++) {
			streamMsg = new VideoMessage(src);
			streamMsg.streamId = event.streamId;
			for (int dest : m_world.GetChildren(StreamThing.GetStreamIdFromNodeId(src.getID()))) {
				streamMsg.destStreamNodeId= dest;
				if (m_output.size() <= m_queuesize) {
					m_output.add(streamMsg);
				} else {
					System.out.println("dropped a packet");
				}
			}
			EDSimulator.add(streamRate/1000, new VideoTransportEvent(), src, pid);
		}
		
		
	}

	public void transportVideoMessages(Node src, int pid) {
		VideoMessage msg;
		while ((msg = m_output.poll()) != null) {
			m_transport.send(src, StreamThing.GetNodeFromNodeId(StreamThing.m_streamIdToNodeId.get(msg.destStreamNodeId)), msg, pid);
		}
	}
	
	public StreamState setState(StreamState newstate) {
		StreamState oldState = m_currentState;
		m_currentState = newstate;
		return oldState;
	}
	
	public void processVideoMessage(Node node, VideoMessage msg) {
		// TODO: check if msg is intended for me
		m_buffer.add(msg);
		
		switch (m_currentState) {
		case IDLE: 
			// queue should be empty - nothing to do
			m_buffer.clear();
			break;
		case VIEW:
			// expect incoming msgs - no need to process, perhaps measure something? 
			consumeVideo(node, msg.streamId);
			break;
		case VIEWnFORWARD:
			// measure something? and send stuff!
			forwardData(node);
			consumeVideo(node, msg.streamId);
			break;
		default:
			// naaeh	
		}
	}
	
	private void forwardData(Node node) {
		VideoMessage msg = null; // TODO: something similar to that above
		System.out.println("I get to forward stuffs!");
	}
	
	private void consumeVideo(Node node, int streamId) {
		for (VideoMessage msg : m_buffer) {
			System.out.println(node.getID() + " consuming video msg: " + msg.id);
		}
	}
}
