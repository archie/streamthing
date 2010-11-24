package eu.emdc.streamthing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import eu.emdc.streamthing.message.Message;
import eu.emdc.streamthing.message.MessageType;
import eu.emdc.streamthing.message.TriggerMessage;
import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dynamics.DynamicNetwork;
import peersim.edsim.EDSimulator;
import peersim.transport.Transport;

public class Initialiser implements Control {

	/** The protocol identifier of the StreamThing protocol */
	int streamThingPid;
	private Queue<TriggerMessage> events;
	
	public Initialiser(String prefix) {
		this.streamThingPid = Configuration.getPid(prefix + ".protocol");
		this.events = readFromFile(Configuration.getString(prefix
				+ ".eventsfile"));
	}

	public boolean execute() {
		// Node node = Network.get(CommonState.r.nextInt(Network.size()));
		if (this.events.size() > 0 && CommonState.getTime() == this.events.peek().triggerTime) {
			TriggerMessage trigger = this.events.remove();
			EDSimulator.add(0, new Message(MessageType.JOIN, null), trigger.source,
					streamThingPid);
		}
			
		return false;
	}

	private Queue<TriggerMessage> readFromFile(String filename) {
		Queue<TriggerMessage> events = new LinkedList<TriggerMessage>();
		
		// while read file
		Node currentNode = CommonState.getNode();
		long eventTime = 10L;
		
		TriggerMessage msg = new TriggerMessage(MessageType.JOIN, currentNode,
				eventTime);
		
		events.add(msg);
		// end file reading
		
		return events;
	}
}
