package eu.emdc.streamthing.message;

import peersim.core.Node;

public class Message {
	
	public MessageType type;
	public int id;
	public Node source;
	public String description; 
	
	public Message(MessageType type) {
		this(type, null);
	}
	
	public Message(MessageType type, Node source) {
		this.type = type;
		this.source = source;
	}
	
	@Override
	public String toString() {
		String s = "[Message] " + type.toString();
		if (source != null) 
			s += " " + source.getID();
		
		return s;
	}
	
	@Override
	public boolean equals(Object o) {
		return ((Message)o).id == this.id;
	}
}
