package peersim.pastry;

import java.math.BigInteger;

//__________________________________________________________________________________________________
/**
 *  Some utility and mathematical function to work with numbers and strings.
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
public class Util {

    //______________________________________________________________________________________________
    /**
     * Given two numbers, returns the length of the common prefix, i.e. how
     * many digits (in the given base) have in common from the leftmost side of
     * the number
     * @param b1 BigInteger
     * @param b2 BigInteger
     * @return int
     */
    public static final int prefixLen(BigInteger b1, BigInteger b2) {

        String s1 = Util.put0(b1);
        String s2 = Util.put0(b2);

        int i = 0;
        for(i = 0; i<s1.length(); i++) {
          if (s1.charAt(i)!=s2.charAt(i))
              return i;
        }

        return i;
    }

    //______________________________________________________________________________________________
    /**
     * return true if b (normalized) starts with c
     * @param b BigInteger
     * @param c char
     * @return boolean
     */
    public static final boolean startsWith(BigInteger b, char c) {
       String s1 = put0(b);
       return (s1.charAt(0)==c);
    }

    //______________________________________________________________________________________________
    /**
     * return the distance between two number, that is |a-b|.
     * no checking is done.
     * @param a BigInteger
     * @param b BigInteger
     * @return BigInteger
     */
    public static final BigInteger distance(BigInteger a, BigInteger b) {
    return a.subtract(b).abs();
    }

    //______________________________________________________________________________________________
    /**
     * given a point (center), returns true if the second parameter (near) has less distance from
     * the center respect with the 3rd point (far)
     * @param center BigInteger
     * @param near BigInteger
     * @param far BigInteger
     * @return boolean
     */
    public static final boolean nearer(BigInteger center, BigInteger near, BigInteger far) {
     return  distance(center,near) .compareTo(  distance(center,far) ) < 0;
    }


    //______________________________________________________________________________________________
    /**
     * Given b, normalize it and check if char c is at specified position
     * @param b BigInteger
     * @param position int
     * @param c char
     * @return boolean
     */
    public static final boolean hasDigitAt(BigInteger b, int position, char c) {
       String s1 = Util.put0(b);
       return (s1.charAt(position)==c);
    }

    //______________________________________________________________________________________________
    /**
     * max between a and b
     * @param a int
     * @param b int
     * @return int
     */
    public static final int max(int a, int b) {
        return a>b?a:b;
    }

    //______________________________________________________________________________________________
    /**
     * min between a and b
     * @param a int
     * @param b int
     * @return int
     */
    public static final int min(int a, int b) {
        return a<b?a:b;
    }


    //______________________________________________________________________________________________
    /**
     * convert a BigInteger into a String, by considering the current BASE, and by leading all
     * needed non-significative zeroes in order to reach the canonical length of a nodeid
     * @param b BigInteger
     * @return String
     */
    public static final String put0(BigInteger b) {
        if (b==null) return null;
      String s = b.toString(MSPastryCommonConfig.BASE).toLowerCase();
      while (s.length() < MSPastryCommonConfig.DIGITS ) s = "0" + s;
      return s;
    }



    //______________________________________________________________________________________________
    public static final char[] DIGITS = new char[] {'0', '1', '2', '3', '4', '5', '6',
                                  '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    /**
     * convert a cipher in the equivalent int value '0'-->0, ... 'f' --> 15
     * @param c char
     * @return int
     */
    public static final int charToIndex(char c) {
         switch (c) {
         case '0': return 0;
         case '1': return 1;
         case '2': return 2;
         case '3': return 3;
         case '4': return 4;
         case '5': return 5;
         case '6': return 6;
         case '7': return 7;
         case '8': return 8;
         case '9': return 9;

         case 'A': return 10;
         case 'a': return 10;
         case 'B': return 11;
         case 'b': return 11;
         case 'C': return 12;
         case 'c': return 12;
         case 'D': return 13;
         case 'd': return 13;
         case 'E': return 14;
         case 'F': return 15;
         case 'e': return 14;
         case 'f': return 15;
         }
         return 0;
     }


     //_____________________________________________________________________________________________
     /**
      * 2^i
      * @param i int i must be a non-negative number
      * @return int 2^i
      */
     public static final int pow2(int i) {
         if (i<0) return 0;
         int result = 1;
         for (int k = 0; k < i; k++)
             result *= 2;
         return result;
     }
     //_____________________________________________________________________________________________


} // End of class
//______________________________________________________________________________________________
