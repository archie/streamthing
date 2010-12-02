package peersim.pastry;

import peersim.core.*;
import peersim.config.Configuration;
import peersim.edsim.EDSimulator;
import java.math.*;

/**
 * This "test" control generates random traffic between random nodes (source and destination).
 * It was created for test and statistical analysis purpose
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


//______________________________________________________________________________________________
public class TrafficGenerator implements Control {

    //______________________________________________________________________________________________
    /**
     * MSPastry Protocol to act
     */
    private final static String PAR_PROT = "protocol";


    /**
     * MSPastry Protocol ID to act
     */
    private final int pid;


    //______________________________________________________________________________________________
    public TrafficGenerator(String prefix) {
        pid = Configuration.getPid(prefix + "." + PAR_PROT);

    }

    //______________________________________________________________________________________________
    /**
     * generates a random lookup message, by selecting randomly the destination.
     * @return Message
     */
    private Message generateLookupMessage() {
            Message m = Message.makeLookUp("Automatically Generated Traffic");
            m.timestamp = CommonState.getTime();

            if (CommonState.r.nextInt(100) < 100)
                m.dest = new BigInteger(MSPastryCommonConfig.BITS, CommonState.r);
             else
                m.dest = ((MSPastryProtocol) (Network.get(CommonState.r.nextInt(
                        Network.size())).getProtocol(pid))).nodeId;

            return m;

        }


    //______________________________________________________________________________________________
    /**
     * every call of this control generates and send a random lookup message
     * @return boolean
     */
    public boolean execute() {

        Node start;
        do {
          start = Network.get(CommonState.r.nextInt(Network.size()));
        }  while (( start==null)||(!start.isUp())) ;
        EDSimulator.add(0, generateLookupMessage(), start, pid);

        return false;
    }

    //______________________________________________________________________________________________


} // End of class
//______________________________________________________________________________________________
