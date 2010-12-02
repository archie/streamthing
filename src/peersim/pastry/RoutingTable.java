package peersim.pastry;

//______________________________________________________________________________________________
/**
 * Gives an implementation for the rounting table component of a patry node
 *
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */

import java.math.BigInteger;

public class RoutingTable implements Cloneable{

    //______________________________________________________________________________________________
    /**
     * Use this to indicates a entry in the table is not filled
     */
    public static final BigInteger EMPTY = null;

    //______________________________________________________________________________________________
    /**
     * all these methods are public in order to provide the fastest possible access
     */
    public BigInteger[][] table = null; //table[i][j] = EMPTY indicates empty cell
    public int rows = 0;
    public int cols = 0;

    //______________________________________________________________________________________________
    public BigInteger get(int rows, int cols) {
        this.cols = cols;
        this.rows = rows;
        return table[rows][cols];
    }

    //______________________________________________________________________________________________
    public void set(int row, int column, BigInteger value) {
     table[row][column] = value;
    }

    //______________________________________________________________________________________________
    /**
     * instanciates a new empty routing table with the specified size
     * @param rows int
     * @param cols int
     */
    public RoutingTable(int rows, int cols) {
      this.rows = rows;
      this.cols = cols;
      table = new BigInteger[rows][cols];
      for (int i = 0; i < rows; i++)
          for (int j = 0; j < cols; j++)
           table[i][j] = EMPTY;
    }


    //______________________________________________________________________________________________
    private RoutingTable() {
    }

    //______________________________________________________________________________________________
    /**
     * Provide direct access to the item of the MSPastry routing table, by
     * selecting the associated entry o fthe table with the given prefix
     * length and the given next cipher
     *
     * @param prefixlen int
     * @param nextChar char
     * @return long
     */
    public BigInteger accessItem(int prefixlen, char nextChar) {
        return table[prefixlen][ Util.charToIndex(nextChar)];
    }

    //______________________________________________________________________________________________
    public Object clone() {
        RoutingTable dolly = new RoutingTable();
        dolly.rows = this.rows;
        dolly.cols = this.cols;
        dolly.table = new BigInteger[rows][cols];
        for (int i = 0; i < this.rows; i++) {
            //dolly.table[i] = new BigInteger[cols];
            for (int j = 0; j < this.cols; j++)
             dolly.table[i][j] = this.table[i][j];
        }
        return dolly;
    }

    //______________________________________________________________________________________________
    public void copyRowFrom(RoutingTable otherRT , int i) {

         for (int col = 0; col<cols;col++) {
             this.table[i][col] = otherRT.table[i][col];
         }
    }

    //______________________________________________________________________________________________
    /**
     * Given a non-null Node Id, it will be removed from the table.
     *
     * @param b BigInteger node to remove from the routing table. if null is specified
     * this method does nothing
     * @return boolean true is the node was in the table (and then correctly removed), false if
     * the node is not in the table. in both cases the node is removed from the table.
     */
    public boolean  removeNodeId(BigInteger b) {
        if (b==null)return false;
        for (int i = 0; i < this.rows; i++)
            for (int j = 0; j < this.cols; j++) {
                if (b.equals(table[i][j])) table[i][j] = null;
                return true;
            }
       return false;
    }

    //______________________________________________________________________________________________
    /**
     * given a NodeId value, it returns an hexadecimal representation and truncate it
     * on the 4th cipher, for instance truncateNodeId(new BigInteger(0xABCDEF0123)) returns "abcd-".
     * This utility was thought only for debug purpose
     * @param b BigInteger
     * @return String
     */
    public static final String truncateNodeId(BigInteger b) {
        if (b == null)
            return "     ";
        return Util.put0(b).substring(0, 4) + "-";
    }

    //______________________________________________________________________________________________
    public String toString(BigInteger nodeId) {
        return "nodeId=" + truncateNodeId(nodeId) + "\n" + this.toString();
    }

    //______________________________________________________________________________________________
    /**
     * print a string representation of the table
     * @return String
     */
    public String toString() {

        String result = "+--------------------------------------------------------------------------------------------------------------------+\n";
        String row = "     ";
        for (int j = 0; j < cols; j++)
            row = row + "[  " + Util.DIGITS[j] + "  ]";
        result = result + row + "\n--------------------------------------------------------------------------------------------------------------------\n";
        for (int i = 0; i < 5; i++) {
            row = "row" + i + " ";
            for (int j = 0; j < cols; j++)
                row = row + "[" + truncateNodeId(table[i][j]) + "]";
            result = result + row + "\n";
        }
        return result;
    }
    //______________________________________________________________________________________________

} // End of class
//______________________________________________________________________________________________
