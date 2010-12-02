package peersim.pastry;

import java.math.*;

/**
 *
 * Message class provide all functionalities to magage the various messages, principally LOOKUP
 * messages (messages from application level sender destinated to another application level).<br>
 *
 * Types Of messages:<br>
 * (application messages)<BR>
 * - MSG_LOOKUP: indicates that the body Object containes information to application level of the
 * recipient<BR>
 * <br>
 * (service internal protocol messages)<br>
 * - MSG_JOINREQUEST: message containing a join request of a node, the message is passed between
 * many pastry nodes accorting to the protocol<br>
 * - MSG_JOINREPLY: according to protocol, the body transport information related to a join reply message <br>
 * - MSG_LSPROBEREQUEST:according to protocol, the body transport information related to a probe request message  <br>
 * - MSG_LSPROBEREPLY: not used in the current implementation<br>
 * - MSG_SERVICEPOLL: internal message used to provide cyclic cleaning service of dead nodes<br>
 *
 * The body for message types MSG_JOINREQUEST and MSG_JOINREPLY if defined by the class
 * Message.BodyJoinRequestReply<br>
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
//______________________________________________________________________________________
public class Message {

    //______________________________________________________________________________________
    public static class BodyJoinRequestReply {
        //Routing table of the joiner, that is built step-by step, from the other nodes
        RoutingTable rt;

        /**
         * Use joiner only in case of a JoinRequest
         */
        java.math.BigInteger joiner;

        /**
         * Use leafset only in case of a JoinReply
         */
        LeafSet ls;

        /**
         * Creates an empty Body For a message of type MSG_JOINREQUEST or type MSG_JOINREPLY
         */
        public BodyJoinRequestReply() {
            rt = null;
            joiner = null;
            ls = null;
        }
    }

    //______________________________________________________________________________________

    /**
     * internal generator for unique ISs
     */
    private static long ID_GENERATOR = 0;

    /**
     * Tha trace vector has this limit size
     */
    public static final int MAX_TRACK = 20;

    /**
     * Message Type
     */
    public static final int MSG_LOOKUP         = 0;

    /**
     * Message Type
     */
    public static final int MSG_JOINREQUEST    = 1;

    /**
     * Message Type
     */
    public static final int MSG_JOINREPLY      = 2;

    /**
     * Message Type
     */
    public static final int MSG_LSPROBEREQUEST = 3;

    /**
     * Message Type
     */
    public static final int MSG_LSPROBEREPLY   = 4;

    /**
     * Internal Message: polling cleaner
     */
    public static final int MSG_SERVICEPOLL   = 5;

    /**
     * Identify the type of this message
     */
    public int messageType = MSG_LOOKUP;



    //______________________________________________________________________________________________
    /**
     * This Object contains the body of the message, no matter what it contains
     */
    public Object body = null;

    /**
     * ID of the message. this is automatically generated univocally, and should not change
     */
    public long id;

    /**
     * Recipient address of the message
     */
    public BigInteger dest;

    /**
     * Source address of the message: has to be filled ad application level
     */
    public BigInteger src;


    /**
     * Available to conunt the number of hops the message did.
     */
    protected int nrHops = 0;

    /**
     * current size of the tracks vector
     */
    protected int trackSize = 0;

    /**
     * Available to contains the path of the message
     */
    protected BigInteger[] tracks = null;

    /**
     * Available to contains the timestamp of the (creation date of the) message
     */
    protected long timestamp = 0;

    //______________________________________________________________________________________________
    /**
     * Creates a lookup message with the specified body
     *
     * @param body Object body to assign (shallow copy)
     */
    public Message(Object body) {
        this(MSG_LOOKUP,body);
    }

    //______________________________________________________________________________________________
    /**
     * Creates an empty message by using default values (message type = MSG_LOOKUP
     * and <code>new String("")</code> value for the body of the message)
     */
    public Message() {
        this(MSG_LOOKUP,"");
    }

    //______________________________________________________________________________________________
    /**
     * Creates an empty message by using default values (message messageType = MSG_LOOKUP and null
     * value for the body of the message)
     *
     * @param messageType int type of the message
     * @param body Object body to assign (shallow copy)
     */
    public Message(int messageType, Object body) {
        this.id = (ID_GENERATOR++);
        this.tracks = new BigInteger[MAX_TRACK];
        this.messageType = messageType;
        this.body = body;
    }


    //______________________________________________________________________________________________
    /**
     * Encapsulates the creation of a join request
     * @param body Object
     * @return Message
     */
    public static final Message makeJoinRequest(Object body) {
        return new Message(MSG_JOINREQUEST, body);
    }

    //______________________________________________________________________________________________
    /**
     * Encapsulates the creation of a join request
     * @param body Object
     * @return Message
     */
    public static final Message makeLookUp(Object body) {
        return new Message(MSG_LOOKUP, body);
    }

    //______________________________________________________________________________________________
    /**
     * returns a ";" separated list of the tracks vector
     * @param header boolean if true, even an initial information header is printed
     * @return String
     */
    public String traceToString(boolean header) {
      if (header) {
          String s = "";
          for (int i = 0; i<trackSize;i++) {
              s = s + RoutingTable.truncateNodeId(tracks[i]) + ";";
          }
          return s + " (" + nrHops+ " hops)";
      }
      return traceToString(true);
  }

  //______________________________________________________________________________________________
  /**
   * returns a ";" separated list of the tracks vector
   * @return String
   */
  public String traceToString() {
       if (trackSize == 0)
            return "Track of message [" + id + "]: <EMPTY>";
       String s = "";
       for (int i = 0; i<trackSize;i++) {
           s = s + RoutingTable.truncateNodeId(tracks[i]) + ";";
       }
       return "Track of message [" + id + "]: " + s + " (" + nrHops+ " hops)";
   }


   //______________________________________________________________________________________________
   public String toString() {
      String s = "[ID="+id+"][DEST=" + dest + "]";
      return s + "[Type=" + messageTypetoString() + "] BODY=(...)";
   }
   //______________________________________________________________________________________________
   public Message copy() {
       Message dolly = new Message();
       dolly.messageType = this.messageType;
       dolly.dest = this.dest;
       dolly.body = this.body; // deep cloning?
       // track, hops NOT copied

       return dolly;

   }

   //______________________________________________________________________________________________
   public String messageTypetoString() {
       switch (messageType) {
       case MSG_LOOKUP: return "MSG_LOOKUP";
       case MSG_JOINREQUEST: return "MSG_JOINREQUEST";
       case MSG_JOINREPLY: return "MSG_JOINREPLY";
       case MSG_LSPROBEREQUEST: return "MSG_LSPROBEREQUEST";
       case MSG_LSPROBEREPLY: return "MSG_LSPROBEREPLY";
       case MSG_SERVICEPOLL: return "MSG_SERVICEPOLL";
       default : return ""+messageType;
       }
   }
}


