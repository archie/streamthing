package eu.emdc.streamthing;

import java.util.LinkedList;
import java.util.Queue;

import eu.emdc.streamthing.stats.Debug;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Fallible;
import peersim.core.Network;
import peersim.core.Node;
import peersim.dynamics.NodeInitializer;
import peersim.edsim.EDSimulator;

public class Initialiser implements Control {

	/** The protocol identifier of the StreamThing protocol */

	private static final String EVENTS = ".eventsfile";
	private static final String PAR_INIT = "init";
	
	private int streamThingPid;
	private boolean firstNode = true;
	private Queue<StreamEvent> events;
	private int initNodeCount = 0;
	
	protected final NodeInitializer[] inits;

	public Initialiser(String prefix) {
		this.streamThingPid = Configuration.getPid(prefix + ".protocol");

		Object[] tmp = Configuration.getInstanceArray(prefix + "." + PAR_INIT);
		inits = new NodeInitializer[tmp.length];
		for (int i = 0; i < tmp.length; ++i) {
			//System.out.println("Inits " + tmp[i]);
			inits[i] = (NodeInitializer) tmp[i];
		}
		
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
				Node node = (Node) Network.prototype.clone();
				for (int j = 0; j < inits.length; ++j) {
					inits[j].initialize(node);
				}
				Network.add(node);
				
				EDSimulator.add(0, event, node, streamThingPid);

				break;
			case FAIL:
				
				for (int i = 0; i < Network.size(); i++)
				{
					Node n = Network.get (i);
					if (n.getID() == StreamThing.GetNodeIdFromStreamId(event.GetNodeId()))
					{
						//Network.remove(i);
						n.setFailState(Fallible.DEAD);
						//Debug.control("NetworkControl: Removing node " + i + " for streamId:" + event.GetNodeId());
						break;
					}
				}
				break;
			default: /* all other events we can just go ahead and schedule */
				
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
