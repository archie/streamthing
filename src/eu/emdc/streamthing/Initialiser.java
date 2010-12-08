package eu.emdc.streamthing;

import java.util.LinkedList;
import java.util.Queue;

import eu.emdc.streamthing.stats.Debug;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDSimulator;

public class Initialiser implements Control {

	/** The protocol identifier of the StreamThing protocol */

	private static final String EVENTS = ".eventsfile";
	private int streamThingPid;
	private boolean firstNode = true;
	private Queue<StreamEvent> events;

	public Initialiser(String prefix) {
		this.streamThingPid = Configuration.getPid(prefix + ".protocol");

		EventHelper eventHelper = new EventHelper();
		eventHelper.InitialiseEvents(Configuration.getString(prefix + EVENTS));

		this.events = eventHelper.GetEventQueue();

	}

	public boolean execute() {
		if (this.events.size() > 0
				&& CommonState.getTime() == this.events.peek()
						.GetExecutionTime()) {
			StreamEvent event = this.events.remove();

			switch (event.GetEventType()) {
			case JOIN:
				Node node;
				if (!firstNode) {
					node = (Node) Network.prototype.clone();
					Network.add(node);
				} else {
					node = Network.get(0);
					firstNode = false;
				}

				EDSimulator.add(0, event, node, streamThingPid);

				break;
			case FAIL:
				
				for (int i = 0; i < Network.size(); i++)
				{
					Node n = Network.get (i);
					if (n.getID() == StreamThing.GetNodeIdFromStreamId(event.GetNodeId()))
					{
						Network.remove(i);
						Debug.control("NetworkControl: Removing node "
								+ i + " for streamId:" + event.GetNodeId());
						break;
					}
				}
				break;
			default: /* all other events we can just go ahead and schedule */
				System.out.println(StreamThing.GetNodeIdFromStreamId(event.GetNodeId()));
				
				EDSimulator.add(0, event, getNode(StreamThing.GetNodeIdFromStreamId(event.GetNodeId())), streamThingPid);
				break; 
			}
		}

		return false;
	}

	private Node getNode(long nodeID) {
		for (int i = 0; i < Network.size(); i++) {
			if (Network.get(i).getID() == nodeID)
				return Network.get(i);
		}

		return null;
	}
}
