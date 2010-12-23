package eu.emdc.streamthing;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import peersim.core.CommonState;
import peersim.core.Node;
import peersim.edsim.EDSimulator;
import peersim.transport.Transport;

import eu.emdc.streamthing.message.TransportWithDelayEvent;
import eu.emdc.streamthing.message.VideoPublishEvent;
import eu.emdc.streamthing.message.VideoMessage;
import eu.emdc.streamthing.stats.MessageStatistics;
import eu.emdc.streamthing.transport.PacketLoss;
import eu.emdc.streamthing.transport.TransportControl;

public class StreamManager {
	private Queue<VideoMessage> m_buffer; // needs to be made global
	
	private Map<Integer, StreamData> m_streams = new HashMap<Integer, StreamData>();
	private TransportControl m_transport;
	//private VideoBuffer<VideoMessage> m_output;
	//private int m_queuesize;
	private int m_uploadCapacity;
	
	class StreamData {
		public int duration;
		public int rate;
		public long started;
		public StreamData(int duration, int rate, long started) {
			this.duration = duration;
			this.rate = rate;
			this.started = started;
		}
	}
	
	public StreamManager(TransportControl transport, int uploadCapacity) {
		m_transport = transport;
		//m_queuesize = 5;
		//m_output = new VideoBuffer<VideoMessage>(m_queuesize);
		m_buffer = new LinkedList<VideoMessage>(); 
		m_uploadCapacity = uploadCapacity;
		//System.out.println("Upload capacity is: " + m_uploadCapacity);
	}
	
	public void publishNewStream(StreamEvent pubEvent) {
		m_streams.put(pubEvent.GetEventParams().get(0).intValue(),
				new StreamData(pubEvent.GetEventParams().get(1).intValue(), 
						pubEvent.GetEventParams().get(2).intValue(), CommonState.getTime()));
		
	}
	
	public void startStream(Node src, int pid, int streamId) {
		VideoPublishEvent pve = new VideoPublishEvent();
		pve.streamId = streamId;
		streamVideo(src, pve, pid);
	}

	
	public void streamVideo(Node src, VideoPublishEvent event, int pid) {
		
		int streamNodeId = StreamThing.GetStreamIdFromNodeId(src.getID());
		if (streamNodeId == -1) // Lag from failure
			return;
		//System.out.println("Stream Node ID " + streamNodeId + " streaming video with stream " + event.streamId );
		List<Integer> children = StreamThing.m_videoStreamIdToMulticastTreeMap.get(event.streamId).GetChildren(streamNodeId);
		StreamData streamData = m_streams.get(event.streamId);
		if (children != null)
		{
			sendData(src, event.streamId, streamData.rate, children, pid);
		}
		if ((CommonState.getTime() + 1000/streamData.rate) < (streamData.started + streamData.duration)) {
			EDSimulator.add(1000/streamData.rate, event, src, pid);
		}
	}

	
	public void processVideoMessage(Node node, VideoMessage msg, int pid) {
		m_buffer.add(msg);
		int streamNodeId = StreamThing.GetStreamIdFromNodeId(node.getID());
		List<Integer> children = StreamThing.m_videoStreamIdToMulticastTreeMap.get(msg.streamId).GetChildren(streamNodeId);
		
		// should I forward too?
		if (children != null && children.size() > 0) { 
			sendData(node, msg.streamId, msg.streamRate, children, pid);
			consumeVideo(node, msg.streamId);
		} else {	
			consumeVideo(node, msg.streamId);
		}
		
	}
	
	private void sendData(Node node, int streamId, int streamRate, List<Integer> children, int pid) {
		VideoMessage streamMsg = null;

		for (int dest : children) 
		{
			streamMsg = new VideoMessage(StreamThing.GetStreamIdFromNodeId(node.getID()));
			streamMsg.streamId = streamId;
			streamMsg.destStreamNodeId= dest;
			streamMsg.streamRate = streamRate;
			
			m_transport.addToQueue(node, streamMsg);
			
			if (m_transport.getQueueSize() == 1) 
			{
				EDSimulator.add(1000/m_uploadCapacity, new VideoTransportEvent(), node, pid);
			}
		}
		
	}
	
	private void consumeVideo(Node node, int streamId) {
		for (VideoMessage msg : m_buffer) {
			long latency = CommonState.getTime() - msg.sent;
			MessageStatistics.latencyNode(msg.source, latency);
			MessageStatistics.latencyStream(streamId, latency);
		}
		m_buffer.clear();
	}
}
