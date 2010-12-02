package eu.emdc.streamthing;

import eu.emdc.streamthing.message.VideoMessage;
import peersim.core.Node;
import peersim.transport.Transport;

public class VideoCreator {
	
	private float m_streamID; 
	private float m_streamDuration; /* timeunits */
	private float m_streamRate; /* packets / thousand_timeunits */
	private NodeWorld m_world;
	private Transport m_transport;
	
	public VideoCreator(NodeWorld world, Transport transport, StreamEvent pubEvent) {
		m_world = world;
		m_transport = transport;
		/* parse pubEvent */
		m_streamID = pubEvent.GetEventParams().get(0);
		m_streamDuration = pubEvent.GetEventParams().get(1);
		m_streamRate = pubEvent.GetEventParams().get(2);
	}
	
	public void streamVideo(Node src, int pid) {
		VideoMessage streamMsg;
		/*
		 * TODO: check max capacity (node config) 
		 * TODO: check video length
		 */
		for (int i = 0; i < 20; i++) {
			streamMsg = new VideoMessage(src);
			streamMsg.stream_id = 1;
			for (Node dest : m_world.getChildren())
				m_transport.send(src, dest, streamMsg, pid);
		}
	}

}
