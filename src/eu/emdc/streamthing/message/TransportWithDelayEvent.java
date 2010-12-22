package eu.emdc.streamthing.message;

import peersim.core.Node;

public class TransportWithDelayEvent {
	public Node src;
	public Node dest;
	public VideoMessage msg;
	public int pid;
}
