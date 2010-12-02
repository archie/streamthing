package peersim.pastry;

import peersim.core.Control;
import peersim.util.IncrementalStats;
import peersim.core.Network;
import peersim.config.Configuration;
import peersim.core.CommonState;


//______________________________________________________________________________________________
public class MSPastryObserver implements Control {

    //______________________________________________________________________________________________
    /**
     * keep statistics of the number of hops of every message delivered.
     */
    public static IncrementalStats hopStore = new  IncrementalStats();

    /**
     * keep statistics of the time every every message needed for delivery.
     */
    public static IncrementalStats timeStore = new  IncrementalStats();

    /** Parameter of the protocol we want to observe */
    private static final String PAR_PROT = "protocol";

    //______________________________________________________________________________________________
    /** Protocol id */
    private int pid;

    /** Prefix to be printed in output */
    private String prefix;

    //______________________________________________________________________________________________
    public MSPastryObserver(String prefix) {
        this.prefix = prefix;
        pid = Configuration.getPid(prefix+"."+PAR_PROT);
    }

    //______________________________________________________________________________________________
    /**
     * print the statistical snapshot of the current situation
     * @return boolean
     */
    public boolean execute() {

        int sz = Network.size();
        for(int i = 0; i<Network.size();i++)
            if (!Network.get(i).isUp()) sz--;

        String s = String.format("[time=%d]:[with N=%d current nodes UP] [%f average hops] [%d msec average transimission time] ",
        CommonState.getTime(),
        sz,
        hopStore.getAverage(),
        (int)timeStore.getAverage()
               );

         System.err.println(s);

        //hopStore.reset();
        return false;
    }
    //______________________________________________________________________________________________
} // enf of class
//______________________________________________________________________________________________
