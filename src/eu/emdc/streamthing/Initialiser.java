package eu.emdc.streamthing;

import java.util.LinkedList;
import java.util.Queue;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDSimulator;

public class Initialiser implements Control {

	/** The protocol identifier of the StreamThing protocol */
	
	private static final String EVENTS = ".eventsfile";
	int streamThingPid;
	private Queue<StreamEvent> events;

	public Initialiser(String prefix) {
		this.streamThingPid = Configuration.getPid(prefix + ".protocol");
		
		EventHelper eventHelper = new EventHelper();
		eventHelper.InitialiseEvents(Configuration.getString(prefix + EVENTS));
		
		this.events = eventHelper.GetEventQueue();

	}

	public boolean execute() {
		if (this.events.size() > 0
				&& CommonState.getTime() == this.events.peek().GetExecutionTime ()) {
			StreamEvent event = this.events.remove();

			switch (event.GetEventType ()) {
			case JOIN:
				Node node = (Node) Network.prototype.clone();
				Network.add(node);
				EDSimulator.add(0, event, node, streamThingPid);
				break;
			case FAIL:
				Network.remove(event.GetNodeId ());
				System.err.println("LogControl: Removing node " + event.GetNodeId());
				break;
			case LEAVE:
				EDSimulator.add(0, event, Network.get(event.GetNodeId ()), streamThingPid);
				break;
			default:
				node = null;
			}

		}

		return false;
	}
}
