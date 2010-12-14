package eu.emdc.streamthing;

import java.util.LinkedList;
import java.util.Queue;

import peersim.core.Node;
import peersim.edsim.EDSimulator;
import peersim.transport.Transport;

import eu.emdc.streamthing.message.PublishVideoEvent;
import eu.emdc.streamthing.message.VideoMessage;


public class StreamManager {
	private StreamState m_currentState = StreamState.IDLE;
	private Queue<VideoMessage> m_buffer; // needs to be made global
	
	private int m_streamID;
	private int m_streamDuration; /* timeunits */
	private int m_streamRate; /* packets / thousand_timeunits */
	private NodeWorld m_world;
	private Transport m_transport;
	private Queue<VideoMessage> m_output;
	private int m_uploadCapacity;
	private int m_queuesize;
	
	public StreamManager(NodeWorld world, Transport transport,
			StreamEvent pubEvent, int uploadCapacity) {
		m_world = world;
		m_transport = transport;
		m_output = new LinkedList<VideoMessage>();
		m_uploadCapacity = uploadCapacity;

		// parse pubEvent
		m_streamID = pubEvent.GetEventParams().get(0).intValue();
		m_streamDuration = pubEvent.GetEventParams().get(1).intValue();
		m_streamRate = pubEvent.GetEventParams().get(2).intValue();

		m_queuesize = m_uploadCapacity / m_streamRate;
		m_buffer = new LinkedList<VideoMessage>(); // FIFO
	}
	
	public void scheduleStream(Node src, int pid) {
		PublishVideoEvent pve = new PublishVideoEvent();
		for (int i = 0; i < m_streamDuration; i++) {
			EDSimulator.add(i, pve, src, pid);
		}
	}

	public void streamVideo(Node src, int pid) {
		VideoMessage streamMsg;

		for (int i = 0; i < m_streamRate; i++) {
			streamMsg = new VideoMessage(src);
			streamMsg.stream_id = (int) m_streamID;
			for (int dest : m_world.getChildren()) {
				//streamMsg.dest = dest;
				if (m_output.size() < m_queuesize) {
					m_output.add(streamMsg);
					// schedule events to empty queue acc to bandwidth
					int bandwidth = 0;
					EDSimulator.add(bandwidth, new VideoTransportEvent(), src, pid);
				} else {
					// count dropped messages
				}
			}
		}
	}

	public void transportVideoMessages(Node src, int pid) {
		VideoMessage msg;
		while ((msg = m_output.poll()) != null) {
			m_transport.send(src, msg.dest, msg, pid);
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
			consumeVideo(node);
			break;
		case FORWARD:
			// send stuff to this node's children
			forwardData(node);
			break;
		case VIEWnFORWARD:
			// measure something? and send stuff!
			forwardData(node);
			break;
		default:
			// naaeh	
		}
	}
	
	private void forwardData(Node node) {
		VideoMessage msg = null;
		while ((msg = m_buffer.poll()) != null) {
			/* 
			 * for all children
			 *   forward msg
			 */
		}
	}
	
	private void consumeVideo(Node node) {
		for (VideoMessage msg : m_buffer) {
			System.out.println(node.getID() + " consuming video msg: " + msg.id);
		}
	}
}
