package eu.emdc.streamthing;

import java.util.LinkedList;
import java.util.Queue;

import peersim.core.Node;

import eu.emdc.streamthing.message.VideoMessage;


public class StreamManager {
	private StreamState m_currentState = StreamState.IDLE;
	private NodeWorld m_world;
	private Queue<VideoMessage> m_buffer;
	
	public StreamManager(NodeWorld world) {
		m_world = world;
		m_buffer = new LinkedList<VideoMessage>(); // FIFO
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
