package peersim.pastry;

import peersim.core.CommonState;
import peersim.config.Configuration;
import peersim.core.Network;
import java.math.BigInteger;

//______________________________________________________________________________________________
/**
 * This control initializes the whole network (that was already created by peersim) by assigning a
 * unique NodeId randomly generated, to every node (it does nothing else).
 * <p>Title: MSPASTRY</p>
 *
 * <p>Description: MsPastry implementation for PeerSim</p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: The Pastry Group</p>
 *
 * @author Elisa Bisoffi, Manuel Cortella
 * @version 1.0
 */
public class CustomDistribution implements peersim.core.Control {

    //______________________________________________________________________________________________
    private static final String PAR_PROT = "protocol";

    private int protocolID;
    private UniformRandomGenerator urg;


    //______________________________________________________________________________________________
    public CustomDistribution(String prefix) {

        protocolID = Configuration.getPid(prefix + "." + PAR_PROT);
        urg = new UniformRandomGenerator(MSPastryCommonConfig.BITS, CommonState.r);
    }


    //______________________________________________________________________________________________
    /**
     * Scan over the nodes in the network and assign a randomly generated NodeId in the space
     * 0..2^BITS, where BITS is a parameter from the pastry protocol (usually 128)
     * @return boolean always false
     */
    public boolean execute() {
       BigInteger tmp;
       for (int i = 0; i < Network.size(); ++i) {
           tmp = urg.generate();
           ((MSPastryProtocol)(Network.get(i).getProtocol(protocolID))).setNodeId(tmp);
       }

        return false;
    }

    //______________________________________________________________________________________________
} // End of class
