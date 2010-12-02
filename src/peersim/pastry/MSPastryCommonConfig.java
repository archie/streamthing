package peersim.pastry;

/**
 * Fixed Parameters of a pastry network. They have a default value and can be configured at
 * startup of the network, once only.
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
public class MSPastryCommonConfig {

  public static final int DIGITS = 32;          /*                 default 32 */
  public static final int BITS  = 128;          /*                 default 128*/

  public static       int B      = 4;           /*                 default   4*/
  public static       int BASE   = 16;          /*   = 2^B         default  16*/

  public static       int L      = 32;          /*  =BITS/B        default  32*/

  public static final boolean DEBUG = true;

  /**
   * short information about current mspastry configuration
   * @return String
   */
  public static String info() {
      return String.format("[B=%d][L=%d][BASE=%d][BITS=%d][DIGITS=%d]", B, L, BASE, BITS, DIGITS);
  }

}
