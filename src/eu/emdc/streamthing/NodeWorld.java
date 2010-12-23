package eu.emdc.streamthing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Network;
import peersim.core.Node;

/**
 * Maintains the multicast tree(s)
 *
 */
public class NodeWorld {
	
	public Integer MAX_CHILDREN = 10;

	private Map<Integer, Integer> m_parentMap = new HashMap<Integer, Integer> ();
	private Map<Integer, List<Integer> > m_childrenMap = new HashMap<Integer, List<Integer> > ();
	//private List<Integer> m_listOfBuckets = new ArrayList<Integer>();
	private Integer m_videoStreamId;
	private Integer m_sourceNodeStreamId;
	private int m_uploadRateOfStream;
	
	public NodeWorld (int videoStreamId, int sourceNodeStreamId, float capacity, int uploadRateOfStream)
	{
		m_videoStreamId = videoStreamId;
		m_sourceNodeStreamId = sourceNodeStreamId;
		List<Integer> temp = new ArrayList<Integer> ();
		m_childrenMap.put(m_sourceNodeStreamId, temp);
		m_parentMap.put (m_sourceNodeStreamId, -1);
		m_uploadRateOfStream = uploadRateOfStream;
		
		System.out.println(sourceNodeStreamId + " has capacity " + capacity);
		System.out.println("Creating NW with root: " + sourceNodeStreamId + " " + videoStreamId);
	}
	
	public void AddNode (int newNodeStreamId, float capacity)
	{
		// Iterate through MTree to find location
		// For now, we do round robin, later, we might want to do a random join
		
		//System.err.println("In " + m_videoStreamId);
		int streamIndex = -1;
		for (;;)
		{
			int x = CommonState.r.nextInt(m_parentMap.size ());
			//	System.err.println(newNodeStreamId + " is going to join with loc " + x);
			Iterator<Entry<Integer, Integer>> iter = m_parentMap.entrySet().iterator();
			while (x != 0)
			{
				//System.err.println("Iterating " + x);
				if (iter.hasNext ())
				{
					Entry<Integer, Integer> entry = iter.next();
					x--;
				}
			}
			
			Entry<Integer, Integer> entry = iter.next();
			
			streamIndex = entry.getKey();
			
			//System.err.println("Found " + streamIndex);
			Node potentialParent = StreamThing.GetNodeFromStreamId(streamIndex);
			
			// Node has failed/left,
			if (potentialParent == null)
			{
				// It's not the publisher
				if (streamIndex != m_sourceNodeStreamId)
					continue;
				else
					break; // Is the publisher. Forget it.
			}
			
			StreamThing st = (StreamThing) potentialParent.getProtocol(Configuration.lookupPid("streamthing"));
			
			if (st == null){
				// Not sure if this should happen, but playing safe
				System.err.println("Fatal error. Random ST instance is null?");
			}
				
			 // Double check for the parent map
			if (m_parentMap.containsKey(streamIndex) && (st.TotalAmountOfUpload() + m_uploadRateOfStream < NodeConfig.GetUploadCapacityForNode(streamIndex)))
			{
				//System.out.println("correcto: found node:" + streamIndex + " streamId:" + m_videoStreamId + " can haz:" + st.TotalAmountOfUpload());
				break;
			}
			else
			{
				continue;
			}
		}
		
		if (streamIndex == -1)
		{
			System.err.println("Error, no stream node found to join?");
		}
		int bucket = streamIndex;
		m_parentMap.put(newNodeStreamId, bucket);
		List<Integer> tempvect =  m_childrenMap.get (bucket);
		tempvect.add(newNodeStreamId);
		m_childrenMap.put (bucket, tempvect);
	
		m_childrenMap.put (newNodeStreamId, new ArrayList<Integer> ());
	}
	
	public void RemoveNodeGraceful (int nodeToBeRemovedStreamId)
	{
		if (!m_parentMap.containsKey(nodeToBeRemovedStreamId) && !m_childrenMap.containsKey(nodeToBeRemovedStreamId))
		{
			return;
		}

		// Publisher dies
		if (m_parentMap.get(nodeToBeRemovedStreamId) == -1)
		{
			m_parentMap.remove(nodeToBeRemovedStreamId);
			m_childrenMap.remove (nodeToBeRemovedStreamId);
			return;
		}
		
		// If someone still expects the publisher to be around
		if (!m_parentMap.containsKey(m_sourceNodeStreamId))
		{
			return;
		}
		
		//System.err.println("Error: Not handling node removal case");
		//System.out.println("Removing " + nodeToBeRemovedStreamId + " from " + m_videoStreamId);
		// First correct the parent
		int parent = m_parentMap.get (nodeToBeRemovedStreamId); // Get parent
		
		//System.out.println("Parent of " + nodeToBeRemovedStreamId + " is " + parent);
		List<Integer> tempvect = m_childrenMap.get (parent);						// Get parent's children
		
		if (tempvect == null)
			tempvect = new ArrayList<Integer>();
		
		for (int i =0; i < tempvect.size (); i++)
		{
			// Remove node from parent's children vect
			if (tempvect.get (i) == nodeToBeRemovedStreamId)
			{
				tempvect.remove(i);
				break;
			}	
		}
		m_childrenMap.put(parent, tempvect);										// Update the children vect
		m_parentMap.remove (nodeToBeRemovedStreamId);								// Remove the node from the parent map
		
		// Now we need to handle the children of the node
		List<Integer>  childvect = m_childrenMap.get (nodeToBeRemovedStreamId);		// Get node's children
		
		if (childvect == null)
			childvect = new ArrayList<Integer> ();
		
		int numOfKids = childvect.size ();
		
		
		for (int i = 0; i < childvect.size (); i++)
		{
			m_parentMap.remove(childvect.get(i));									// Remove each child from Parent map
		}
		
		// Parent of node which is leaving wil have an empty bucket now
		
		// Now find the orphaned children some new parents
		for (int i = 0 ; i < childvect.size (); i++)
		{
			// For now, we do round robin, later, we might want to do a random join
			int newNodeStreamId = childvect.get (i);
			int k = 0;
			
			int streamIndex = -1;
			for (;;)
			{
				int x = CommonState.r.nextInt(m_parentMap.size ());
				//System.err.println(newNodeStreamId + " is going to join with loc " + x);
				Iterator<Entry<Integer, Integer>> iter = m_parentMap.entrySet().iterator();
				while (x != 0)
				{
					//System.err.println("Iterating " + x);
					if (iter.hasNext ())
					{
						Entry<Integer, Integer> entry = iter.next();
						x--;
					}
				}
				
				Entry<Integer, Integer> entry = iter.next();
				
				streamIndex = entry.getKey();
				
				
				Node potentialParent = StreamThing.GetNodeFromStreamId(streamIndex);

				// Node has failed/left
				if (potentialParent == null)
				{
					// It's not the publisher
					if (streamIndex != m_sourceNodeStreamId)
						continue;
					else
						break; // Is the publisher. Forget it.
				}
				//System.err.println("Found " + streamIndex);
				StreamThing st = (StreamThing) potentialParent.getProtocol(Configuration.lookupPid("streamthing"));
				
				if (st == null){
					// Not sure if this should happen, but playing safe
					System.err.println("Fatal error. Random ST instance is null?");
				}
					
				 // Double check for the parent map
				if (m_parentMap.containsKey(streamIndex)) //&& (st.TotalAmountOfUpload() + m_uploadRateOfStream < st.m_nodeConfig.GetUploadCapacityForNode(streamIndex)))
				{
					//System.out.println("correcto: found node:" + streamIndex + " streamId:" + m_videoStreamId + " can haz:" + st.TotalAmountOfUpload());
					break;
				}
				else
				{
					continue;
				}
			}
			
			int bucket = streamIndex;
			
			m_parentMap.put(newNodeStreamId, bucket);
			List<Integer> tempvect1 =  m_childrenMap.get (bucket);
			tempvect1.add(newNodeStreamId);
			m_childrenMap.put (bucket, tempvect1);
			
			//System.out.println("AbandondedStreamNode " + newNodeStreamId + " has a new parent " + bucket);
		}
		
		m_childrenMap.remove (nodeToBeRemovedStreamId);
	}
	
	public int getRootNode() {
		return m_sourceNodeStreamId;
	}
	
	public List<Integer> GetChildren (int queryingNodeStreamId)
	{
		return m_childrenMap.get (queryingNodeStreamId);
	}
	
	public Integer GetParent (int queryingNodeStreamId)
	{
		if (m_parentMap.containsKey(queryingNodeStreamId))
		{
			return m_parentMap.get(queryingNodeStreamId);
		}
		else
		{
			return null;
		}
	}
}
