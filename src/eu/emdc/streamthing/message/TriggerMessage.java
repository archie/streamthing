package eu.emdc.streamthing.message;

import peersim.core.Node;

public class TriggerMessage extends Message{

	public long triggerTime;
	
	public TriggerMessage(MessageType type, Node source, long time) {
		super(type, source);
		this.triggerTime = time;
	}
	
}
