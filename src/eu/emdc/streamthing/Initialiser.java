package eu.emdc.streamthing;

import eu.emdc.streamthing.message.Message;
import eu.emdc.streamthing.message.MessageType;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDSimulator;

public class Initialiser implements Control {
	
	/** The protocol identifier of the StreamThing protocol */
    int streamThingPid;

    public Initialiser(String prefix) {
        streamThingPid = Configuration.getPid( prefix+".protocol");
    }

    public boolean execute() {        
    		Node randomNode = Network.get(CommonState.r.nextInt(Network.size()));

    		EDSimulator.add(0, new Message(MessageType.JOIN, CommonState.getNode()), randomNode, streamThingPid);
    		
        return false;
    }
}
