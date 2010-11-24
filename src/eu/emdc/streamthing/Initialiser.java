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
	int streamThingPid;
	private Queue<StreamEvent> events;

	public Initialiser(String prefix) {
		this.streamThingPid = Configuration.getPid(prefix + ".protocol");
		this.events = readFromFile(Configuration.getString(prefix
				+ ".eventsfile"));
	}

	public boolean execute() {
		if (this.events.size() > 0
				&& CommonState.getTime() == this.events.peek().triggerTime) {
			StreamEvent event = this.events.remove();

			switch (event.type) {
			case JOIN:
				Node node = (Node) Network.prototype.clone();
				Network.add(node);
				event.node = node;
				EDSimulator.add(0, event, node, streamThingPid);
				break;
			case FAIL:
				Network.remove(event.nodeID);
				break;
			case LEAVE:
				event.node = Network.get(event.nodeID);
				EDSimulator.add(0, event, event.node, streamThingPid);
				break;
			default:
				node = null;
			}

		}

		return false;
	}

	private Queue<StreamEvent> readFromFile(String filename) {
		Queue<StreamEvent> events = new LinkedList<StreamEvent>();

		// while read file
		int currentNode = 0;
		long eventTime = 10L;

		StreamEvent msg = new StreamEvent(StreamEventType.JOIN, currentNode,
				eventTime);

		events.add(msg);
		// end file reading

		return events;
	}
}
