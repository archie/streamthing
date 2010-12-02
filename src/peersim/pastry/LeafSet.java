package peersim.pastry;

import java.math.BigInteger;

//__________________________________________________________________________________________________
/**
 *
 * LeafSet class encapsulate functionalities of a Leaf Set table in a Pastry Node, allowing
 * automatic "intellingent" adding of the entries, and facilitating extraction of information
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
public class LeafSet implements Cloneable {

    //______________________________________________________________________________________________
    /**
     * indicates not filled positions
     */
    private static final BigInteger EMPTY = null;

    //______________________________________________________________________________________________
    /**
     * ordered array of the nodeIds inserted closest to and lower than the nodeId
     * left[0] is the closest (and less) of the current nodeId, and so on...
     */
    private BigInteger[] left = null;

    /**
     * ordered array of the nodeIds inserted closest to and higher than the nodeId
     * right[0] is the closest (and greater) of the current nodeId, and so on...
     */
    private BigInteger[] right = null;

    /**
     * total size of the leaf set
     */
    private int size = 0;

    /**
     * size of both left and right part of the leaf set.
     */
    public int hsize = 0;

    /**
     * pivot nodeId, this is needed in order to know how to organize adding/positioning/searching of
     * the entries of the leaf set
     */
    public BigInteger nodeId = null;


    //______________________________________________________________________________________________
    /**
     * Not allowed to call default (without parameters) constructor!
     */
    private LeafSet() {}

    //______________________________________________________________________________________________
    /**
     * shortcut constructor to use integers instead of BigIntegers.
     * @param myNodeId long
     * @param size int
     */
    public LeafSet(long myNodeId, int size) {
        this(new BigInteger(""+myNodeId), size);
    }

    //______________________________________________________________________________________________
    /**
     * Creates a new Leaf Set by pivoting it with the specified nodeId, and with the desired size
     * of the vector. Half of the size will be used to store nodes lessed than the pivot nodeId,
     * the other half for the greater entries. Note: is size is an odd number, (size+1) will always
     * be considered
     * @param myNodeId BigInteger the pivot nodeId of the leafset, i.e. the nodeid of the pastry
     * node owner
     * @param size int must be > 0, and possibily an even number
     */
    public LeafSet(BigInteger myNodeId, int size) {

        nodeId = myNodeId;

        size = size + (size%2);
        hsize = size/2;
        this.size = size;
        left = new BigInteger[hsize];
        right = new BigInteger[hsize];
        for (int i = 0; i < hsize; i++)
         left[i]=right[i]=EMPTY;
    }


    //______________________________________________________________________________________________
    private final boolean eq(BigInteger b1, BigInteger b2) {
        if (b1==null) return (b2==null);
        return b1.equals(b2);
    }
    private final int cmp(BigInteger b1, BigInteger b2) {
        if (b1==null) if (b2==null) return 0; else return -1;
        if (b2==null) return +1; else return Util.put0(b1).compareTo(Util.put0(b2));
    }

    //______________________________________________________________________________________________
    /**
     * returns EMPTY if the leaf set does not contains the specified Key.
     * returns the position in one of the two arrays of the keyToFind otherwise.
     * to establish if the keyToFind is located in left or in right array, simply check
     * if newNode > keyToFind (right) or not (left)
     * @param keyToFind long
     * @return int
     */
    private int indexOf(BigInteger keyToFind) {
        if (keyToFind==null) return -1;
        if (cmp(keyToFind,keyToFind) > 0) { //cerca a destra
            for (int index = 0; (index < hsize); index++) {
               if (right[index].equals(keyToFind)) return index;
            }
            return -1;
        }
        //cerca a sinistra
        for (int index = 0; index < hsize; index++){
            if (eq(left[index],keyToFind)) return index;
        }
        return -1;
    }


    //______________________________________________________________________________________________
    /**
     * make a shift of an array v by one position, starting from the element with index "pos"
     * (pos is included), with the purpose to create a new (empty) available slot at the index "pos"
     * Note: the last element of the array is lost
     */
    private void shift(BigInteger[] v, int pos) {
        for(int i = hsize-1; i>pos; i--)
            v[i] = v[i-1];
    }



    //______________________________________________________________________________________________
    private boolean removeNode(BigInteger b, BigInteger[] v) {
        int pos = indexOf(b);
        if (pos==-1) return false;
        for (int i = pos; i<v.length-1; i++) {
            v[i] = v[i+1];
        }
        v[v.length-1] = EMPTY;
        return true;
    }

    //______________________________________________________________________________________________
    /**
     * permanently removes the specified NodeId from this Leaf Set.
     * @param b BigInteger
     * @return boolean true is some element is removed, false if the element does not exists
     */
    public boolean removeNodeId(BigInteger b) {
          if (b==null)return false;
          if (cmp(b,nodeId)<0) return removeNode(b, left);
          return removeNode(b, right);
    }


    //______________________________________________________________________________________________
    /**
     * if returns "Empty" indicates: DO NOT (RE)INSERT!!!
     * @param n long
     * @return int
     */
    private int correctRightPosition(BigInteger n) {
        for(int i = 0; i<hsize;i++) {
            if (right[i]==EMPTY) return i;
            if (eq(right[i],n)) return -1;
            if (cmp(right[i],n)>0) return i;
        }
        return hsize;
    }


    /**
     * Empty indicates: DO NOT (RE)INSERT!!!
     * @param n long
     * @return int
     */
    private int correctLeftPosition(BigInteger n) {
        for(int i = 0; i<hsize;i++) {
            if (left[i]==EMPTY) return i;
            if (eq(left[i],n)) return -1;
            if (cmp(left[i],n)<0) return i;
        }
        return hsize;
    }




    //______________________________________________________________________________________________
    private void pushToRight(BigInteger newNode) {
       int index =  correctRightPosition(newNode);
       if (index==-1) return;
       if (index==hsize) return;
       shift(right,index);
       right[index]=newNode;
    }

    private void pushToLeft(BigInteger newNode) {
       int index =  correctLeftPosition(newNode);
       if (index==-1) return;
       if (index==hsize) return;
       shift(left,index);
       left[index]=newNode;
    }


    private int countNonEmpty(BigInteger[]a) {
        int count = 0;
        for(count = 0; (count<a.length)&&(a[count]!=EMPTY);count++) /*NOOP*/ ;
        return count;
    }

    //______________________________________________________________________________________________
    /**
     * shortcut for  push(new BigInteger(""+newNode));
     * @param newNode long
     */
    public void push(long newNode) {
        push(new BigInteger(""+newNode));
    }


    //______________________________________________________________________________________________
    /**
     * push into the leafset the specified node, by according the properties specified by the
     * mspastry protocol
     *
     * @param newNode long
     */
    public void push(BigInteger newNode) {
      if (eq(newNode,nodeId)) return;
      if (cmp(newNode,nodeId)>0)
          pushToRight(newNode);
      else
          pushToLeft(newNode);
    }

    //______________________________________________________________________________________________
    /**
     * returns true iff whe specified node is found in the table
     * @param node BigInteger
     * @return boolean
     */
    public boolean containsNodeId(BigInteger node) {
     return indexOf(node) != -1;
    }


    //______________________________________________________________________________________________
    /**
     * returns the lesser nodeid stored
     * @return BigInteger
     */
    private BigInteger min() {
        if (countNonEmpty(left)==0) return nodeId;
        return left[countNonEmpty(left)-1];
    }

    /**
     * returns the greater nodeid stored
     * @return BigInteger
     */
    private BigInteger max() {
        if (countNonEmpty(right)==0) return nodeId;
        return right[countNonEmpty(right)-1];
    }

    //______________________________________________________________________________________________
    /**
     * returns true if key is between the leftmost and the rightmost.
     * it does not require that key is contained in the table, only requires that the key is
     * greater-equal than the min nodeid stored and lesser-equal than the max nodeid stored.
     * Note: this.ls.encompass(this.ls.nodeid) always returns true, in all cases.
     * @param k BigInteger
     * @return boolean
     */
    public boolean encompass(BigInteger k) {

        if (min().compareTo(k)  > 0) return false;
        if (max().compareTo(k)  < 0) return false;

        return true;
    }


    //______________________________________________________________________________________________
    /**
     * Outputs an (ordered, from min to max) array of all nodes in the leaf set.
     * The actual pivot nodeid is not included.
     * @return BigInteger[]
     */
    public BigInteger[] listAllNodes() {
      int numLeft = countNonEmpty(left);
      int numRight = countNonEmpty(right);
      BigInteger[] result = new BigInteger[numLeft+numRight];
      for(int i = 0; i<numLeft;i++)
          result[i] = left[i];
      for(int i = 0; i<numRight;i++)
          result[numLeft+i] = right[i];
       return result;
    }


    //______________________________________________________________________________________________
    /**
     * produces an exact deep clone of this Object, everything is copied
     * @return Object
     */
    public Object clone() {
        LeafSet dolly = new LeafSet();
        dolly.nodeId = this.nodeId;
        dolly.size = this.size;
        dolly.hsize = this.hsize;
        dolly.left = this.left.clone();
        dolly.right = this.right.clone();
        return dolly;
    }

    //______________________________________________________________________________________________
    /**
     * shortcut to base-representation specifier
     */
    public static final int HEX = 16;

    /**
     * shortcut to base-representation specifier
     */
    public static final int DEC = 10;

    /**
     * shortcut to base-representation specifier
     */
    public static final int NIB = 4;

    /**
     * shortcut to base-representation specifier
     */
    public static final int BIN = 2;

    /**
     * Outputs a representation of this leafset in the form:<BR>
     * <code>[L3;L2;L1;L0]pivot[R0;R1;R2;R3]</code><BR>
     * each entry is represented only partially, to allow a shorter
     * represantation (i.e. is cut after the 4th cipher, e.g.: "4eb0-")
     * @return String
     */
    public String toString() {

      String l = "[XX]";
      for(int i = 0; (i<hsize)&&(left[i]!=EMPTY);i++)
       l = l.replace("XX", RoutingTable.truncateNodeId(left[i])+";XX");

      l = l.replace(";XX","");
      l = l.replace("XX","");

      String p = "{"+ RoutingTable.truncateNodeId(nodeId)  +"}";

      String r = "[XX]";
      for(int i = 0; (i<hsize)&&(right[i]!=EMPTY);i++)
      r = r.replace("XX", (RoutingTable.truncateNodeId(right[i]))+";XX");
      r = r.replace(";XX","");
      r = r.replace("XX","");

      return l+p+r;
    }
    //______________________________________________________________________________________________


} // End of class
//______________________________________________________________________________________________
