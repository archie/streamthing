package peersim.pastry;

import peersim.config.*;
import peersim.core.*;
import java.util.Comparator;
import peersim.transport.Transport;

/**
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
public class StateBuilder implements peersim.core.Control {

    private static final String PAR_PROT = "protocol";
    private static final String PAR_TRANSPORT = "transport";

    private String prefix;
    private int mspastryid;
    private int transportid;

    public StateBuilder(String prefix) {
        this.prefix = prefix;
        mspastryid = Configuration.getPid(this.prefix + "." + PAR_PROT);
        transportid = Configuration.getPid(this.prefix + "." + PAR_TRANSPORT);
    }

    //______________________________________________________________________________________________
    public final MSPastryProtocol get(int i) {
        return ((MSPastryProtocol) (Network.get(i)).getProtocol(mspastryid));
    }

    //______________________________________________________________________________________________
    public final Transport getTr(int i) {
        return ((Transport) (Network.get(i)).getProtocol(transportid));
    }

    //______________________________________________________________________________________________
    public void fillLevel(int curLevel, int begin, int end, int nodo) {

        int B = MSPastryCommonConfig.B;
        int BASE = MSPastryCommonConfig.BASE;
        int sz = Network.size();

        if (curLevel >= 10)
            return;

        if (curLevel >= MSPastryCommonConfig.BITS / B)
            return;
        /**
         * supponiamo che tutti i livelli precedenti sono gi� stati riempiti.
         * lavoriamo solo nell'intervallo [begin..end[
         * riempiamo prima la riga curLevel
         * copiamo la riga a tutti gli altri.
         * ...poi
         * chiamate ricorsive sui sottorappresentanti
         */
        long[] minlatencies = new long[BASE]; // in associazione con i nodeid
        int[] minindeces = new int[BASE]; // in associazione con i nodeid

        for (int i = 0; i < minlatencies.length; i++)
            minlatencies[i] = Long.MAX_VALUE;

        for (int i = 0; i < 10* BASE; i++) {
            int randomIndex = begin + CommonState.r.nextInt(end - begin);
            long lat = getTr(randomIndex).getLatency(Network.get(nodo),
                    Network.get(randomIndex));
            //o(get(randomIndex).nodeId.toString() +  " --> level " +curLevel);

            int nextch = Util.charToIndex(Util.put0(get(randomIndex).nodeId).charAt(curLevel));
            if (lat < minlatencies[nextch]) {
                minlatencies[nextch] = lat;
                minindeces[nextch] = randomIndex;
                get(nodo).routingTable.set(curLevel, nextch,
                                           get(randomIndex).nodeId);
            }

        }

        for (int i = begin; i < end; i++)
            get(i).routingTable.table[curLevel] = get(nodo).routingTable.table[curLevel].clone();

        int subbegin = begin;
        int subend = begin;
        for (int i = 0; i < BASE; i++) {
            char curChar = Util.DIGITS[i];

            if (!Util.hasDigitAt(get(subbegin).nodeId, curLevel, curChar))
                continue;

           subend = subbegin;

            while ((subend<sz)&&(Util.hasDigitAt(get(subend).nodeId, curLevel, curChar)))
                subend++;


            x("Entering level:" + (curLevel + 1));
            fillLevel(curLevel + 1, subbegin, subend,minindeces[Util.charToIndex(curChar)]);
            x("      Exiting level:" + (curLevel + 1));

            if ( subend >= sz) break;
            subbegin = subend;

        }


    }


    //______________________________________________________________________________________________
    public static void o(Object o) { System.out.println(o);}
    public static void x(Object o) { }

    //______________________________________________________________________________________________
    public boolean execute() {
       // !!! segna tempo iniziale

        /* Sort the network by nodeId (Ascending) */
        //o("SORTING NODES");
        Network.sort(new Comparator() {

            public int compare(Object o1, Object o2) {
                Node n1 = (Node) o1;
                Node n2 = (Node) o2;
                MSPastryProtocol p1 = (MSPastryProtocol) (n1.getProtocol(mspastryid));
                MSPastryProtocol p2 = (MSPastryProtocol) (n2.getProtocol(mspastryid));
                return Util.put0(p1.nodeId).compareTo(Util.put0(p2.nodeId));
                // return p1.nodeId.compareTo(p2.nodeId);
            }

            public boolean equals(Object obj) {
                return compare(this, obj) == 0;
            }
        });

        int sz = Network.size();

        //o("\n\nLISTA ORDINATA:");   for(int i =0; i<sz;i++) o(Util.put0(get(i).nodeId));


        /**
         * la prima riga di ogni nodo � ottenuta scegliendo casualmente il
         * rappresentante di livello 0
         */
        MSPastryProtocol node0 = get(0);

        int rappresentanti[] = new int[MSPastryCommonConfig.BASE];

        int begin = 0;
        int end = 0;
        for (int i = 0; i < MSPastryCommonConfig.BASE; i++) {
            if (begin>=Network.size()) break;

            char curChar = Util.DIGITS[i];

            if (!Util.startsWith(get(begin).nodeId, curChar))
                continue;

            end = begin; //aggiunta successiva

            //qui il primo char di begin(nodeID)=primo char di curChar
            while (((end < Network.size()))&&(Util.startsWith(get(end).nodeId, curChar)))
                end++;

            int randomIndex = begin + CommonState.r.nextInt(end - begin);

            node0.routingTable.table[0][Util.charToIndex(curChar)] = get(randomIndex).nodeId;
            rappresentanti[Util.charToIndex(curChar)] = randomIndex;

            begin = end;

        }


        //x(get(0).routingTable.toString((get(0).nodeId)));


        /**
         * Tutti i nodi condividono sulla RT la riga 0 (calcolata sopra in node0) in comune
         */
        for (int i = 1; i < sz; i++)
            get(i).routingTable.table[0] = node0.routingTable.table[0].clone();

        /*
                prefix = XXXX...  (level=4)
                rappresentante: XXXXFBCD

               isoliamo quelli che iniziano per A, per B per C...
               per A: prendamone al max 10 e teniamo quello (Y) con latency + bassa: XXXXFBCD.RT[..][..] = Y
               (idem x gli altri)

               ora abbiamo completato la riga XXXX?? per il rappresentante --> copia uguale a tutti gli altri

               per ogni rappresentante appena trovato: riapplica metodo, fino al livello (128/b)

         */

        begin = 0;
        end = 0;
        for (int i = 0; i < MSPastryCommonConfig.BASE; i++) {
            if (begin>=Network.size()) break;

            char curChar = Util.DIGITS[i];

            if (!Util.startsWith(get(begin).nodeId, curChar))
                continue;

            while ((end < sz)&&(Util.startsWith(get(end).nodeId, curChar)))
                end++;

            fillLevel(1, begin, end, rappresentanti[Util.charToIndex(curChar)]);

            begin = end;

        }

    // riempimento dei leaf sets
    begin = 0;
    end = 0;
    for (int i = 0; i < MSPastryCommonConfig.BASE; i++) {
        char curChar = Util.DIGITS[i];

        if (!Util.startsWith(get(begin).nodeId, curChar))
            continue;

        end = begin;

        while ((end < sz)&&(Util.startsWith(get(end).nodeId, curChar)))
            end++;

        //[begin..end[
        for (int k = begin; k < end; k++) {
            // set up Li
            MSPastryProtocol n = get(k);

            for (int z = 1; z <= n.leafSet.hsize; z++) {
                if (k - z < 0)
                    break;
                n.leafSet.push(get(k - z).nodeId);
            }

            for (int z = 1; z <= n.leafSet.hsize; z++) {
                if (k + z >= sz)
                    break;
                n.leafSet.push(get(k + z).nodeId);
            }
        }
        if ( end >= sz) break;
        begin = end;

    }


    return false;
    // !!! segna tempo finale-iniziale

} //end execute()

}
