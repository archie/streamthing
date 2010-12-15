package eu.emdc.streamthing;

import java.util.Iterator;
import java.util.Queue;
import java.util.Map.Entry;

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
	private Queue<StreamEvent> events;

	protected final NodeInitializer[] inits;

	public Initialiser(String prefix) {
		this.streamThingPid = Configuration.getPid(prefix + ".protocol");

		Object[] tmp = Configuration.getInstanceArray(prefix + "." + PAR_INIT);
		inits = new NodeInitializer[tmp.length];
		for (int i = 0; i < tmp.length; ++i) {
			// System.out.println("Inits " + tmp[i]);
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
				//System.out.println(event.GetNodeId() + " "+ event.GetEventType());
				EDSimulator.add(0, event, node, streamThingPid);
				break;
			case FAIL:
				// TODO: brutally remove or mark as dead... not sure... if mark
				// as dead
				getNodeFromStreamNodeId(event.GetNodeId()).setFailState(Fallible.DEAD);
				break;
			default: /* all other events we can just go ahead and schedule */
				
				Node toNode = getNodeFromStreamNodeId(event.GetNodeId());
				if (toNode == null) {
					Debug.control("No such node in network. " + event.GetEventType() + " : " + event.GetNodeId());
				} else {
					EDSimulator.add(0, event, toNode, streamThingPid);
				}
				break;
			}
		}

		return false;
	}

	private Node getNodeFromStreamNodeId(int streamNodeID) {
		/*
		System.out.println("ZZ");
		Iterator<Entry<Integer, Long>> iter = StreamThing.m_streamIdToNodeId.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Integer, Long> entry = iter.next();
			System.out.println("in map " + entry.getKey() + " " + entry.getValue());
				
		}*/
		
		if (StreamThing.m_streamIdToNodeId.containsKey(streamNodeID)) {
			long nodeId = StreamThing.m_streamIdToNodeId.get(streamNodeID);
			for (int i = 0; i < Network.size(); i++) {
				if (Network.get(i).getID() == nodeId)
					return Network.get(i);
			}
		}
		return null;
	}
}
