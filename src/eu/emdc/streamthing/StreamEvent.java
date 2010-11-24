package eu.emdc.streamthing;

import peersim.core.Node;


public class StreamEvent {
	long triggerTime;
	int nodeID;
	Node node;
	StreamEventType type;

	public StreamEvent(StreamEventType type, int nodeID, long time) {
		this.nodeID = nodeID;
		this.triggerTime = time;
		this.type = type;
	}
	
	public StreamEvent(StreamEventType type, Node node, long time) {
		this.node = node;
		this.type = type;
		this.triggerTime = time;
	}
}
