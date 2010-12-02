package peersim.pastry;

/**
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

import java.math.*;

import peersim.config.*;
import peersim.core.*;
import peersim.edsim.*;
import peersim.transport.*;
import java.util.Comparator;

//__________________________________________________________________________________________________
public class MSPastryProtocol implements Cloneable, EDProtocol {
    //______________________________________________________________________________________________
    /**
     * Event Handler container for managing the receiving of a message
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
    public static interface Listener {
        /**
         * This method is called every time a message is received
         * @param m Message
         */
        public void receive(Message m);
    }

    /**
     * Listener assingned to the receiving of a message. If null it is not called
     */
    private Listener listener;

    //______________________________________________________________________________________________
    /**
     * allows to change/clear the listener
     * @param l Listener
     */
    public void setListener(Listener l) {
    	listener = l;
    }

    //______________________________________________________________________________________________
    private static final String PAR_TRANSPORT = "transport";
    private static String prefix = null;
    private UnreliableTransport transport;
    private int tid;
    private int mspastryid;
    private boolean cleaningScheduled = false;

    /**
     * allow to call the cleaning service initializer only once
     */
    private static boolean _ALREADY_INSTALLED = false;

    //______________________________________________________________________________________________
    /**
     * nodeId of this pastry node
     */
    public BigInteger nodeId;

    /**
     * routing table of this pastry node
     */
    public RoutingTable routingTable;

    /**
     * leaf set of this pastry node
     */
    public LeafSet leafSet;


    //______________________________________________________________________________________________
    /**
     * Replicate this object by returning an identical copy.
     * it put the eye on the fact that only the peersim initializer call this
     * method and we expects to replicate every time a non-initialized table.
     * Thus the method clone() do not fill any particular field;
     * @return Object
     */
    public Object clone() {

        MSPastryProtocol dolly = new MSPastryProtocol(MSPastryProtocol.prefix);
        dolly.routingTable = (RoutingTable)this.routingTable.clone();
        dolly.leafSet = (LeafSet)this.leafSet.clone();
        //dolly.nodeId is not copied, because ID is unique!
        return dolly;
    }


    //______________________________________________________________________________________________
    /**
     * Used only by the initializer when creating the prototype Every other instance call CLONE to
     * create the new object. clone could not use this constructor, preferring a more quick
     * constructor
     *
     * @param prefix String
     */
    public MSPastryProtocol(String prefix) {
        this.nodeId = null;              // empty nodeId
        MSPastryProtocol.prefix = prefix;

        _init();

        routingTable = new RoutingTable(MSPastryCommonConfig.BITS / MSPastryCommonConfig.B, Util.pow2(MSPastryCommonConfig.B));
        leafSet = new LeafSet(BigInteger.ZERO, MSPastryCommonConfig.L);

        tid = Configuration.getPid(prefix + "." + PAR_TRANSPORT);

    }

    //______________________________________________________________________________________________
    /**
     * This subrouting is called only once and allow to inizialize the internal state of the
     * MSPastreyProtocol. Every node shares the same configuration, so it is sufficient caling
     * ont this this sub in order to set up the configuration
     */
    private void _init() {
        if (_ALREADY_INSTALLED) return;

        int b=0, l=0, base=0;
        final String PAR_B = "B";
        final String PAR_L = "L";


        b = Configuration.getInt(prefix + "." + PAR_B, 4);
        l = Configuration.getInt(prefix + "." + PAR_L, MSPastryCommonConfig.BITS / b);
        base = Util.pow2(b);

        MSPastryCommonConfig.B = b;
        MSPastryCommonConfig.L = l;
        MSPastryCommonConfig.BASE = base;

        e(MSPastryCommonConfig.info()+"\n");

        _ALREADY_INSTALLED = true;
    }

    //______________________________________________________________________________________________
    /**
     * called when a Lookup message is ready to be passed through the upper/application level, by
     * calling the "message received" event handler (listener). It also provide some statistical
     * update
     * @param m Message
     */
    private void deliver(Message m) {
        //statistiche utili all'observer
        MSPastryObserver.hopStore.add(m.nrHops-1);
        long timeInterval = (CommonState.getTime())-(m.timestamp);
        MSPastryObserver.timeStore.add(timeInterval);

        if (listener != null) {
            listener.receive(m);
        }

    }

    //______________________________________________________________________________________________
    /**
     * given one nodeId, it search through the network its node reference, by performing binary serach
     * (we concern about the ordering of the network).
     * @param searchNodeId BigInteger
     * @return Node
     */
    private Node nodeIdtoNode(BigInteger searchNodeId) {
        if (searchNodeId==null) return null;

        int inf = 0;
        int sup = Network.size() - 1;
        int m;

        while (inf <= sup) {
            m = (inf + sup) / 2;

            BigInteger mId = ((MSPastryProtocol) Network.get(m).getProtocol(mspastryid)).nodeId;

            if (mId.equals(searchNodeId))
                return Network.get(m);

            if (mId.compareTo(searchNodeId) < 0)
                inf = m + 1;
            else
                sup = m - 1;
        }

        /**
         * La ricerca binaria � veloce ed efficiente, ma qualche volta pu� capitare che l'array dei
         * nodi di Network non sia ordinato, quindi si applica ora la ricerca sequenziale.
         * Se nemmeno la ricerca sequenziale trova il nodo cercato, viene ritornato null
         */
        BigInteger mId;
        for (int i= Network.size()-1; i >= 0 ; i--) {
              mId = ((MSPastryProtocol) Network.get(i).getProtocol(mspastryid)).nodeId;
             if  (mId.equals(searchNodeId))
                    return Network.get(i);
        }

        return null;
    }

    //______________________________________________________________________________________________
    /**
     * see MSPastry protocol "ReceiveRoute" primitive
     * @param m Message
     */
    public void receiveRoute(Message m) {
        switch (m.messageType) {
        case Message.MSG_LOOKUP:
            deliver(m);
            o(" [rr] Delivered message [m.id="+m.id+"] [src=dest=" + RoutingTable.truncateNodeId(nodeId) + "[in "+ ((CommonState.getTime())-(m.timestamp))+" msecs] ["+m.nrHops+" hops]");
            break;
        case Message.MSG_JOINREQUEST:

            m.messageType = Message.MSG_JOINREPLY;
            // ((Message.BodyJoinRequestReply)m.body).joiner = null; //not necessary anymore
            ((Message.BodyJoinRequestReply)m.body).ls = this.leafSet;
            //  ((Message.BodyJoinRequestReply)m.body).rt = ...LEAVE AS IS...

            transport = (UnreliableTransport) (Network.prototype).getProtocol(tid);
            transport.send(nodeIdtoNode(this.nodeId), nodeIdtoNode(m.dest), m, mspastryid);
            break;

        }
    }

    //______________________________________________________________________________________________
    /**
     * see MSPastry protocol "Route" primitive
     * @param m Message
     * @param srcNode Node
     */
    private void route(Message m, Node srcNode) {

        // u("["+RoutingTable.truncateNodeId(nodeId)+"] received msg:[" + m.id + "] to route, with track: <");        o(m.traceToString(false) + ">");

        BigInteger nexthop = null;

        //leave a track of the transit of the message over this node
        m.nrHops++;
        if(m.trackSize<m.tracks.length)
            m.trackSize++;
        m.tracks[m.trackSize-1]= this.nodeId;

        BigInteger curdist;
        BigInteger[] allNodes;
        BigInteger mindist;



        if (leafSet.encompass(m.dest)) {
            // il nodeID j in Li t.c. |k-j| � minimo
            int near = 0;
            allNodes = leafSet.listAllNodes();

            if (allNodes.length != 0)  {

                mindist = m.dest.subtract(allNodes[near]).abs();
                for (int i = 1; i < allNodes.length; i++) {
                    curdist = m.dest.subtract(allNodes[i]).abs();
                    if (mindist.compareTo(curdist) > 0) {
                        mindist = curdist;
                        near = i;
                    }
                }
                nexthop = allNodes[near];
            } else  nexthop = this.nodeId;

        }
        else {
            int r = Util.prefixLen(m.dest, this.nodeId);
            if (r == MSPastryCommonConfig.DIGITS) {
                deliver(m);
                o("  [route]   Delivered message src=dest=" + RoutingTable.truncateNodeId(nodeId));
                return;
            }

            nexthop = this.routingTable.get(r, Util.charToIndex(Util.put0(m.dest).charAt(r))  );

            if (nexthop == null) {
                //il nodeID j in (Li U Ri) t.c. |k-j| < |k-i| && prefixLen(k,j)>=r

                BigInteger[] l = this.leafSet.listAllNodes();


                for (int jrow = 0; jrow < routingTable.rows; jrow++) {
                    for (int jcol = 0; jcol < routingTable.cols; jcol++) {
                        BigInteger nodejj = routingTable.get(jrow, jcol);
                        if (nodejj!=null)
                         if (cond1(m.dest, this.nodeId,nodejj) &&
                             cond2(m.dest, nodejj, r)) {
                            nexthop = nodejj;
                            break;
                        }
                    }
                }

                if (nexthop == null)
                for (int j = 0; j < l.length; j++) {
                    if (cond1(m.dest, this.nodeId, l[j]) &&
                        cond2(m.dest, l[j], r)) {
                        nexthop = l[j];
                        break;
                    }
                }


            } // end if (nexthop==null)
        }


        o(String.format("[%s].route([type=%s][src:%s][dest:%s][m.id=%d]): [nexthop:%s]",
            RoutingTable.truncateNodeId(nodeId),
            m.messageTypetoString(),
            "", // RoutingTable.truncateNodeId(src.nodeId),
            RoutingTable.truncateNodeId(m.dest),
            m.id,
            RoutingTable.truncateNodeId(nexthop)
                ));

        if ( m.body instanceof Message.BodyJoinRequestReply && false)
         o("m.RT "+((Message.BodyJoinRequestReply)(m.body)).rt);




        /** !!!
         *  (this.nodeId.equals(m.dest)) � troppo limitativo, noi vogliamo vedere se "io" sono
         * quello pi� (numericammente) vicino possibile.
         * Poich� supponiamo di avvicinarci progressivamente, questo si traduce nel controllare
         * se sono pi� vicino dell'ultimo nodo attraversato (m.traks[last])
         *
         * l'hop lo facciamo solo se facendolo... ridurremo la distanza,
         * la distanza tra destinatario e me
         *   rispetto alla distanza fra destinatario e precedente
         */
        if ((m.trackSize > 0) && (nexthop != null)) {
            BigInteger src = m.tracks[m.trackSize-1]; if (!Util.nearer(m.dest,nexthop,src))
            //if (!Util.nearer(m.dest,nexthop,src.nodeId))
             nexthop = this.nodeId;
        }


        if (  (!this.nodeId.equals(nexthop)) && (nexthop != null)) {    //send m to nexthop
            transport = (UnreliableTransport) (Network.prototype).getProtocol(tid);
            transport.send(nodeIdtoNode(this.nodeId), nodeIdtoNode(nexthop), m, mspastryid);
        }
        else receiveRoute(m);


    }

    //______________________________________________________________________________________________
    /**
     * Sort the nodes of the network by its nodeIds
     */
    private void sortNet() {
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
    /**
     * search the node that is nerares than the specified node
     * @param current Node
     * @return Node
     */
    private Node selectNeighbor(Node current) {
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


           long lat = getTr(randomIndex).getLatency(current,Network.get(randomIndex));

           if (lat < minLatency) {
               minLatency = lat;
               seed = randomIndex;
           }
         }

       return Network.get(seed);
   }


    //______________________________________________________________________________________________
    /**
     * Given that this node was correctly initialized (e.g. routing table and  leafset created, and
     * empty) it perform a join requesta to the mspastry according to the protocol specification
     */
    public void join() {
        if (this.nodeId == null) {
            UniformRandomGenerator urg = new UniformRandomGenerator(
                    MSPastryCommonConfig.BITS, CommonState.r);
            this.setNodeId(urg.generate());
            sortNet();
        }

       Message joinrequest = Message.makeJoinRequest(null);
               joinrequest.body = new Message.BodyJoinRequestReply();
       Message.BodyJoinRequestReply body =  (Message.BodyJoinRequestReply )(joinrequest.body);
       body.joiner = this.nodeId;

       body.rt = this.routingTable;

       joinrequest.dest = this.nodeId;

       Node seed = selectNeighbor(nodeIdtoNode(this.nodeId));

       peersim.edsim.EDSimulator.add(0, joinrequest, seed, mspastryid);


    }


    //______________________________________________________________________________________________
    /**
     * shortcut for getting the MSPastry level of the node with index "i" in the network
     * @param i int
     * @return MSPastryProtocol
     */
    public final MSPastryProtocol get(int i) {
        return ((MSPastryProtocol) (Network.get(i)).getProtocol(mspastryid));
    }

    //______________________________________________________________________________________________
    /**
     * shortcut for getting the Transport level of the node with index "i" in the network
     * @param i int
     * @return MSPastryProtocol
     */
    public final Transport getTr(int i) {
        return ((Transport) (Network.get(i)).getProtocol(tid));
    }

    //______________________________________________________________________________________________
    /**
     * This primitive provide the sending of the data to dest, by encapsulating it into a LOOKUP
     * Message
     *
     * @param recipient BigInteger
     * @param data Object
     */
    public void send(BigInteger recipient, Object data) {
    	Message m = new Message(data);
    	m.dest = recipient;
    	m.src = this.nodeId;
    	m.timestamp = CommonState.getTime();

    	/*
    	 * starting by the current pastry node (this.NodeId), until destination
    	*/
      Node me = nodeIdtoNode(this.nodeId);
      EDSimulator.add(0, m, me, mspastryid);
    }

    //______________________________________________________________________________________________
    private static final boolean cond1(BigInteger k, BigInteger i, BigInteger j) {
        return k.subtract(j).abs().compareTo(k.subtract(i).abs()) < 0;
    }

    private static final boolean cond2(BigInteger k, BigInteger j, int r) {
        return Util.prefixLen(k, j) >= r;
    }


    //______________________________________________________________________________________________
    /**
     * @param myNode Node
     * @param myPid int
     * @param m Message
     */
    void performJoinRequest(Node myNode, int myPid, Message m) {
        // aggiungi alla m.rt la riga N di myNode.R,
        // dove commonprefixlen vale n-1
        // (calcolata tra il nodo destinatatio (j) e (il nodeId di myNode)
        MSPastryProtocol myP = ((MSPastryProtocol) myNode.getProtocol(myPid));
        Message.BodyJoinRequestReply body = (Message.BodyJoinRequestReply)m.body;

        if (nodeId.equals(body.joiner)) return;

        int n = Util.prefixLen(nodeId, body.joiner) + 1;

        body.rt.copyRowFrom(myP.routingTable, n);
    }



    //______________________________________________________________________________________________
    /**
     * see MSPastry protocol "performJoinReply" primitive
     */
    private void probeLS() {
        e("probeLS\n");
        BigInteger[] leafs = this.leafSet.listAllNodes();

        for (int i = 0; i < leafs.length; i++) {
            transport = (UnreliableTransport) (Network.prototype).getProtocol(tid);

            Message m = new Message(Message.MSG_LSPROBEREQUEST, null);
            m.dest = this.nodeId; //using m.dest to contain the source of the probe request


            transport.send(nodeIdtoNode(this.nodeId), nodeIdtoNode(leafs[i]), m, mspastryid);
        }

    }


    //______________________________________________________________________________________________
    /**
     * see MSPastry protocol "performJoinReply" primitive
     * @param myNode Node
     * @param myPid int
     * @param m Message
     */
    void performJoinReply(Node myNode, int myPid, Message m) {
        // Ri.add(R u L)           (i = myself)
        // Li.add(L)

        Message.BodyJoinRequestReply reply = (Message.BodyJoinRequestReply) m.body;
        //this.routingTable = (RoutingTable) reply.rt.clone();
        this.routingTable = reply.rt;

        BigInteger[] l = reply.ls.listAllNodes();

        for (int j = 0; j < l.length; j++) {
            int row, col;

            row = Util.prefixLen(this.nodeId, l[j]);
            col = Util.charToIndex(Util.put0(l[j]).charAt(row)); /// prima era:col = Util.charToIndex(Util.put0(l[j]).charAt(row + 1));

            this.routingTable.set(row, col, l[j]);
        }

        // poch� this.leafSet e' vuoto, la add() viene fatta tramite assegnazione diretta.
        this.leafSet = (LeafSet) reply.ls.clone();
        this.leafSet.nodeId = this.nodeId;


        probeLS();

    }
    //______________________________________________________________________________________________
    /**
     * see MSPastry protocol "performLSProbeRequest" primitive
     * @param m Message
     */
    private void performLSProbeRequest(Message m) {

        this.leafSet.push(m.dest);

        int row = Util.prefixLen(this.nodeId, m.dest);
        int col = Util.charToIndex(Util.put0(m.dest).charAt(row)); /// prima era:col = Util.charToIndex(Util.put0(l[j]).charAt(row + 1));

        BigInteger cell = this.routingTable.get(row, col);
        if (cell!=null) {

         transport = (UnreliableTransport) (Network.prototype).getProtocol(tid);

          long oldLat = transport.getLatency(nodeIdtoNode(this.nodeId), nodeIdtoNode(cell));
          long newLat = transport.getLatency(nodeIdtoNode(this.nodeId), nodeIdtoNode(m.dest));
            if ( newLat > oldLat )
                return;
        }

         this.routingTable.set(row, col, m.dest);
    }

    //______________________________________________________________________________________________
    /**
     * the cleaning service is called occasionally in order to remove from the tables of this node
     * failed entrie.
     * @param myNode Node
     * @param myPid int
     * @param m Message
     */
    private void cleaningService(Node myNode, int myPid, Message m) {
        // cleaning tables...

        BigInteger bCheck;
        Node nCheck;
        for (int irow = 0; irow < routingTable.rows; irow++)
         for (int icol = 0; icol < routingTable.cols; icol++) {
             bCheck = routingTable.get(irow, icol);
             nCheck = nodeIdtoNode(bCheck);
             if ((nCheck == null) || (!nCheck.isUp()))
                routingTable.set(irow, icol, null);
         }

        BigInteger[] bCheck2 = leafSet.listAllNodes();
        for (int i = 0; i < bCheck2.length; i++) {
            nCheck = nodeIdtoNode(bCheck2[i]);
            if ((nCheck == null) || (!nCheck.isUp()))
             leafSet.removeNodeId(bCheck2[i]);
        }

        long delay = 1000 + CommonState.r.nextLong(1000);
        EDSimulator.add(delay, m, myNode, myPid);
    }

    //______________________________________________________________________________________________
    /**
     * manage the peersim receiving of the events
     * @param myNode Node
     * @param myPid int
     * @param event Object
     */
    public void processEvent(Node myNode, int myPid, Object event) {


        if (!cleaningScheduled) {
            long delay = 1000 + CommonState.r.nextLong(1000);
            Message service = new Message(Message.MSG_SERVICEPOLL, "");
            service.dest = nodeId;
            EDSimulator.add(delay, service, myNode, myPid);
            cleaningScheduled =true;
        }

        /**
         * Parse message content
         * Activate the correct event manager fot the partiular event
         */
        this.mspastryid = myPid;

        Message m = (Message) event;

        switch (m.messageType) {
        case Message.MSG_LOOKUP:
            route(m, myNode);
            break;

        case Message.MSG_JOINREQUEST:
            performJoinRequest(myNode, myPid, m);
            route(m, myNode);
            break;

        case Message.MSG_JOINREPLY:
            performJoinReply(myNode, myPid, m);
            break;

        case Message.MSG_SERVICEPOLL:
            cleaningService( myNode,  myPid,  m);
            break;
        case Message.MSG_LSPROBEREQUEST:
            performLSProbeRequest(m);
            break;
        }


    }

    //______________________________________________________________________________________________
    /**
     * set the current NodeId
     *
     * @param tmp BigInteger
     */
    public void setNodeId(BigInteger tmp) {
        this.nodeId = tmp;
        leafSet.nodeId = tmp;

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

    //______________________________________________________________________________________________
} // End of class
