package eu.emdc.streamthing.message;

import peersim.core.CommonState;

public class StreamMessage {
	
	public MessageType type;
	public int id;
	public int source;
	public String description; 
	public long sent;
	public int streamId;
	public int streamRate;
	
	
	public StreamMessage(MessageType type) {
		this(type, 0);
	}
	
	public StreamMessage(MessageType type, int source) {
		this.type = type;
		this.source = source;
		this.sent = CommonState.getTime();
	}
	
	@Override
	public String toString() {
		String s = "[Message] " + type.toString();
		if (source != 0) 
			s += " " + source;
		
		return s;
	}
	
	@Override
	public boolean equals(Object o) {
		return ((StreamMessage)o).id == this.id;
	}
}
