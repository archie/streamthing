package eu.emdc.streamthing;

import java.util.Iterator;
import java.util.Queue;
import java.util.Map.Entry;

import eu.emdc.streamthing.stats.Debug;
import eu.emdc.streamthing.stats.LogControl;

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
	private EventHelper eventHelper = new EventHelper();
	private int m_eventsParsed = 0;

	protected final NodeInitializer[] inits;

	public Initialiser(String prefix) {
		this.streamThingPid = Configuration.getPid(prefix + ".protocol");

		Object[] tmp = Configuration.getInstanceArray(prefix + "." + PAR_INIT);
		inits = new NodeInitializer[tmp.length];
		for (int i = 0; i < tmp.length; ++i) {
			// System.out.println("Inits " + tmp[i]);
			inits[i] = (NodeInitializer) tmp[i];
		}

		eventHelper.InitialiseEvents(Configuration.getString(prefix + EVENTS));
	}

	public boolean execute() {
		StreamEvent event; 
		
		if (eventHelper.getNextTime() == 0) {
			LogControl lc = (LogControl) Configuration.getInstance("control.accounting");
			lc.execute();
			return true;
		}
		
		if ((CommonState.getTime() == eventHelper.getNextTime())
				&& m_eventsParsed < eventHelper.m_numEvents)
		{
			//System.out.println("parsing event " + m_eventsParsed + " num events" + eventHelper.m_numEvents);
			if ((event = eventHelper.poll()) != null) 			
				m_eventsParsed++;	
			else 
				return false;
			
			//System.out.println(CommonState.getTime() + " event time " + event.GetExecutionTime());
			
			switch (event.GetEventType()) {
			case JOIN:
				Node node = (Node) Network.prototype.clone();
				
				for (int j = 0; j < inits.length; ++j) {
					inits[j].initialize(node);
				}
				
				if (StreamThing.m_streamIdToNodeId.containsKey(event.GetNodeId())) {
					getNodeFromStreamNodeId(event.GetNodeId()).setFailState(Fallible.OK);
				} else {
					Network.add(node);
				}
				
				EDSimulator.add(0, event, node, streamThingPid);
				break;
			case FAIL:
				//System.err.println("OMGIMMAFAIL: " + event.GetNodeId() + " --- " + StreamThing.m_streamIdToNodeId.get (event.GetNodeId()));
				
				// Clean up the node's data structures:
				Node n = getNodeFromStreamNodeId(event.GetNodeId());
				if(n == null)
				{
					break;
				}
				StreamThing s = (StreamThing) n.getProtocol(Configuration.lookupPid("streamthing"));
				
				s.cleanup();
				StreamThing.m_streamIdToNodeId.remove(event.GetNodeId());
				n.setFailState(Fallible.DOWN);

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
