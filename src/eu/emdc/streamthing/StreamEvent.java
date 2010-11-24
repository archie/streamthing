package eu.emdc.streamthing;

import peersim.core.Node;


public class StreamEvent {
	long triggerTime;
	Node node;
	StreamEventType type;

	public StreamEvent(StreamEventType type, Node node, long time) {
		this.node = node;
		this.triggerTime = time;
		this.type = type;
	}
}
