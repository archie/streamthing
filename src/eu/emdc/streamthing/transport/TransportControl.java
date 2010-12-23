package eu.emdc.streamthing.transport;

import peersim.config.Configuration;
import peersim.core.Node;
import peersim.edsim.EDSimulator;
import eu.emdc.streamthing.StreamThing;
import eu.emdc.streamthing.VideoBuffer;
import eu.emdc.streamthing.VideoTransportEvent;
import eu.emdc.streamthing.message.TransportWithDelayEvent;
import eu.emdc.streamthing.message.VideoMessage;
import eu.emdc.streamthing.stats.MessageStatistics;

public class TransportControl {

	private VideoBuffer<VideoMessage> m_output;
	private int m_uploadCapacity;
	
	public TransportControl(int upcapacity) {
		m_uploadCapacity = upcapacity;
		m_output = new VideoBuffer<VideoMessage>(5);
	}
	
	public void addToQueue(Node src, VideoMessage msg) {
		addToQueue(src, msg, 1000);
	}
	
	public void addToQueue(Node src, VideoMessage msg, int size) {
		if (!m_output.add(msg)) 
		{
			MessageStatistics.droppedNode(StreamThing.GetStreamIdFromNodeId(src.getID()));
			MessageStatistics.droppedStream(msg.streamId);
		}
	}
	
	public int getQueueSize() {
		return m_output.size();
	}
	
	public void transportMessages(Node src, int pid) {
		VideoMessage msg;
		
		msg = m_output.get();
		if (msg != null) {
			if (StreamThing.m_streamIdToNodeId.containsKey(msg.destStreamNodeId))
			{
				Node dest = StreamThing.GetNodeFromNodeId(StreamThing.m_streamIdToNodeId.get(msg.destStreamNodeId));
				StreamThing s = (StreamThing)dest.getProtocol(Configuration.lookupPid("streamthing")); // this is a little redundant
				if (dest != null && s.m_myStreamNodeId != -1) {
					TransportWithDelayEvent e = new TransportWithDelayEvent();
					e.src = src; e.dest = dest; e.msg = msg; e.pid = pid;
					EDSimulator.add(s.latency(src, dest), e, src, pid);
				}
			}
		}
		
		if (m_output.size() > 0) {
			EDSimulator.add(1000/m_uploadCapacity, new VideoTransportEvent(), src, pid);
		}
	}
	
}
