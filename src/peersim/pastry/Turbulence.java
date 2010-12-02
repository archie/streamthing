package peersim.pastry;

import java.util.*;

import peersim.config.*;
import peersim.core.*;
import peersim.transport.Transport;
import peersim.dynamics.NodeInitializer;


//______________________________________________________________________________________________
/**
 *
 * Turbulcen class is only for test/statistical purpose. This Contro provides the oscillating of the
 * network, allowing that every execution of it will result in a node adding or a node failure.
 * The probabilities are configurabily from the parameters p_idle, p_add, p_rem <BR>
 * - p_idle (default = 0) state the probability that the current execution does nothing (i.e. no
 * adding and no failures).<br>
 * - p_add (default = 0.5): states the probability that (id this execution is going to do something
 * : see p_idle parameter) this execution will result in a join request of a new created node
 * - p_rem (deafult = 1-p_add, i.e. default = 0.5): states the probability that (id this execution
 * is going to do something : see p_idle parameter) this execution will result in a failure  of an
 * existing node.<br>
 * p_add and p_rem are mutually exclusive, only one of them can should be specified, the other is
 * calculated by performing the negation (1-p). If both are specified, p_add has precedence.
 * To all probabilities must be assigned value in the real range [0..1]
 * <br>
 * <br>
 * Other parameters:<br>
 * - maxsize (default: infinite): state that no more than this node can be added. if this limit
 * is reached, this execution does not perform any operation<br>
 * - minsize (default: 1): state that no less than this node can be removed. if this limit
 * is reached, this execution does not perform any operation
 *
 *
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

public class Turbulence implements Control {

    //______________________________________________________________________________________________
    private static final String PAR_PROT = "protocol";
    private static final String PAR_TRANSPORT = "transport";
    private static final String PAR_INIT = "init";

    //______________________________________________________________________________________________
    /**
     * specify a minimum size for the network. by default there is no limit
     */
    private static final String PAR_MINSIZE = "minsize";

    /**
     * specify a maximum size for the network.by default there is limit of 1
     */
    private static final String PAR_MAXSIZE = "maxsize";

    /**
     * idle probability
     */
    private static final String PAR_IDLE = "p_idle";

    /**
     * probability to add a node (in non-idle execution)
     */
    private static final String PAR_ADD = "p_add";

    /**
     * probability to fail a node (in non-idle execution). Note: nodes will NOT be removed from the
     * network, but will be set as "DOWN", in order to let peersim exclude automatically the delivering
     * of events destinates to them
     */
    private static final String PAR_REM = "p_rem";

    /** node initializers to apply on the newly added nodes */
    protected NodeInitializer[] inits;

    private String prefix;
    private int mspastryid;
    private int transportid;
    private int maxsize;
    private int minsize;
    private double p_idle;
    private double p_add;

    //______________________________________________________________________________________________
    public Turbulence(String prefix) {
        this.prefix = prefix;
        mspastryid = Configuration.getPid(this.prefix + "." + PAR_PROT);
        transportid = Configuration.getPid(this.prefix + "." + PAR_TRANSPORT);

        minsize = Configuration.getInt(this.prefix + "." + PAR_MINSIZE, 1);
        maxsize = Configuration.getInt(this.prefix + "." + PAR_MAXSIZE, Integer.MAX_VALUE);

        Object[] tmp = Configuration.getInstanceArray(prefix + "." + PAR_INIT);
        inits = new NodeInitializer[tmp.length];
        for (int i = 0; i < tmp.length; ++i)
          inits[i] = (NodeInitializer) tmp[i];


        //probability to do nothing in this execution of the control. By default not
        // "idle" executions (p_idle = 0) are done.
        p_idle = Configuration.getDouble(this.prefix + "." + PAR_IDLE, 0);

        /*
         p_add parameter determines the probability that the current action is adding a node, instead of
         removeing one (1-p_add). it's optional, by default is 0.5 (adopted as default value, or whether
         an invalid probability is specified)
         p_rem parameter determines the probability that the current action is removing a node, instead of
         adding one (1-p_add). default is (1-p_add) (adopted as default value, or whether
         an invalid probability is specified)
         if both p_add and _rem are specified, only p_add is considered and p_rem is ignored.
        */
         p_add = Configuration.getDouble(this.prefix + "." + PAR_ADD, -1.0);
         if ((p_add) < 0 || (p_add > 1)) {
          double p_rem =  Configuration.getDouble(this.prefix + "." + PAR_REM, -1);
          if ((p_rem) < 0 || (p_rem > 1)) p_add = 0.5; else  p_add = 1-p_rem;
         	}

     e(String.format("Turbolence: [p_idle=%f] [p_add=%f] [(min,max)=(%d,%d)]", p_idle, p_add, maxsize, minsize));
    }

    //______________________________________________________________________________________________
    /**
     * shortcut for getting the mspastry level/protocol reference of the specified node number
     * @param i int
     * @return MSPastryProtocol
     */
    private final MSPastryProtocol get(int i) {
        return ((MSPastryProtocol) (Network.get(i)).getProtocol(mspastryid));
    }

    //______________________________________________________________________________________________
    /**
     * shortcut for getting the transport level/protocol reference of the specified node number
     * @param i int
     * @return Transport
     */
    private final Transport getTr(int i) {
            return ((Transport) (Network.get(i)).getProtocol(transportid));
    }


    //______________________________________________________________________________________________
    private void addOneNode() {
        Node newnode = (Node) Network.prototype.clone();
        for (int j = 0; j < inits.length; ++j)
            inits[j].initialize(newnode);
        Network.add(newnode);
    }

    //______________________________________________________________________________________________
    public void sortNet() {
        Network.sort(new Comparator() {
            //______________________________________________________________________________________
            public int compare(Object o1, Object o2) {
                Node n1 = (Node) o1;
                Node n2 = (Node) o2;
                MSPastryProtocol p1 = (MSPastryProtocol) (n1.getProtocol(mspastryid));
                MSPastryProtocol p2 = (MSPastryProtocol) (n2.getProtocol(mspastryid));
                return Util.put0(p1.nodeId).compareTo(Util.put0(p2.nodeId));
                // return p1.nodeId.compareTo(p2.nodeId);
            }
            //______________________________________________________________________________________
            public boolean equals(Object obj) {
                return compare(this, obj) == 0;
            }
            //______________________________________________________________________________________
        });
    }


    //______________________________________________________________________________________________
    private void removeOneNode() {
        int randomIndex;

       do {
          randomIndex =  CommonState.r.nextInt(Network.size());
       } while (!Network.get(randomIndex).isUp() );


       Network.get(randomIndex).setFailState(Node.DOWN);


    }


    //______________________________________________________________________________________________
   private Node selectNeighbor(int index) {
       //scelgo il seed come fatto nello StateBuilder per i rappresentanti
       //il seed sar� quel Node che da m� ha la minor latenza
       int candidates = 10;
       long minLatency = Long.MAX_VALUE;
       int seed = 0;

       for (int i = 0; i < candidates; i++) {
           int randomIndex;
           do {
              randomIndex =  CommonState.r.nextInt(Network.size());
           } while (!Network.get(randomIndex).isUp() );


           long lat = getTr(randomIndex).getLatency(Network.get(index),Network.get(randomIndex));

           if (lat < minLatency) {
               minLatency = lat;
               seed = randomIndex;
           }
         }

       return Network.get(seed);
   }

   //______________________________________________________________________________________________
   public boolean add() {
       // set node id
       // empty tables
       // sort net
       //generate JOIN REQUEST message

       addOneNode();

       int index = -1;

       for (int i = Network.size() - 1; i >= 0; i--)
           if (get(i).nodeId == null) {
               index = i;
               break;
           }

       if (index < 0) {
           e("\nFatal Error Occurred, or empty network!\n");
           return false;
       }

       UniformRandomGenerator urg = new UniformRandomGenerator(MSPastryCommonConfig.BITS, CommonState.r);
       MSPastryProtocol newNode = get(index);
       newNode.setNodeId(urg.generate());

       sortNet();


       // PERFORM JOINING BY SENDING A JOIN REQUEST
       Message joinrequest = Message.makeJoinRequest(null);
               joinrequest.body = new Message.BodyJoinRequestReply();
       Message.BodyJoinRequestReply body =  (Message.BodyJoinRequestReply )(joinrequest.body);
       body.joiner = newNode.nodeId;
       body.rt = newNode.routingTable;

       joinrequest.dest = newNode.nodeId;

       Node seed = selectNeighbor(index);

       peersim.edsim.EDSimulator.add(0, joinrequest, seed, mspastryid);
       return false;
    }

    //______________________________________________________________________________________________
   public boolean rem() {
       removeOneNode();
       return false;
   }


    //______________________________________________________________________________________________
    public boolean execute() {
        o("..... EXECUTING NODE INITIALIZER");

        double dice =  CommonState.r.nextDouble();
        if (dice < p_idle) return false;


        int sz = Network.size();
        for(int i = 0; i<Network.size();i++)
            if (!Network.get(i).isUp()) sz--;

        dice =  CommonState.r.nextDouble();

         // ADDING ONE NODE
        if (dice < p_add) {
            if (sz>=maxsize) return false;
            o("................................ >> ADDING 1");
            return add();
        }

        // REMOVING ONE NODE
        if (sz<=minsize) return false;
        o("................................ >> REMOVING 1");
        return rem();
    }

    //______________________________________________________________________________________________
    /**
     * debug only
     * @param o Object
     */
    private static void e(Object o) { if (MSPastryCommonConfig.DEBUG) System.err.println(o);}
    /**
     * debug only
     * @param o Object
     */
    private static void o(Object o) { if (MSPastryCommonConfig.DEBUG) System.out.println(o);}

    /**
     * debug only
     * @param o Object
     */
    private static void u(Object o) { if (MSPastryCommonConfig.DEBUG) System.out.print(o);}
    //______________________________________________________________________________________________
} // End of class


