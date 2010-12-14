package eu.emdc.streamthing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import peersim.core.Node;

/**
 * Maintains the multicast tree(s)
 *
 */
public class NodeWorld {
	
	public Integer MAX_CHILDREN = 1;

	private Map<Integer, Integer> m_parentMap = new HashMap<Integer, Integer> ();
	private Map<Integer, List<Integer> > m_childrenMap = new HashMap<Integer, List<Integer> > ();
	private List<Integer> m_listOfBuckets = new ArrayList<Integer>();
	private Integer m_videoStreamId;
	private Integer m_sourceNodeStreamId;
	
	public NodeWorld (int videoStreamId, int sourceNodeStreamId, float capacity)
	{
		m_videoStreamId = videoStreamId;
		m_sourceNodeStreamId = sourceNodeStreamId;
		List<Integer> temp = new ArrayList<Integer> ();
		m_childrenMap.put(m_sourceNodeStreamId, temp);
		m_parentMap.put (m_sourceNodeStreamId, -1);
		
		System.out.println(sourceNodeStreamId + " has capacity " + capacity);
		for (int i = 0; i < MAX_CHILDREN; i++)
		{
			m_listOfBuckets.add (sourceNodeStreamId);
		}
		System.out.println("Creating NW with root: " + sourceNodeStreamId + " " + videoStreamId);
	}
	
	public void AddNode (int newNodeStreamId, float capacity)
	{
		// Iterate through MTree to find location
		// For now, we do round robin, later, we might want to do a random join
		int bucket = m_listOfBuckets.get(0);
		m_parentMap.put(newNodeStreamId, bucket);
		List<Integer> tempvect =  m_childrenMap.get (bucket);
		tempvect.add(newNodeStreamId);
		m_childrenMap.put (bucket, tempvect);
		m_listOfBuckets.remove(0);
		System.out.println(newNodeStreamId + " has capacity " + capacity);
		m_childrenMap.put (newNodeStreamId, new ArrayList<Integer> ());
		for (int i = 0; i < MAX_CHILDREN; i++)
		{
			m_listOfBuckets.add (newNodeStreamId);
		}
		
		System.out.println("Adding: " + newNodeStreamId);
		System.out.println("Parent is: " + bucket);
		
		for (int i = 0; i < m_childrenMap.get (bucket).size(); i++)
		{
			System.out.println("With child: " + m_childrenMap.get (bucket).get(i));
		}
	}
	
	public void RemoveNodeGraceful (int nodeToBeRemovedStreamId)
	{
//		System.err.println("Error: Not handling node removal case");
		System.out.println("Removing " + nodeToBeRemovedStreamId + " from " + m_videoStreamId);
		// First correct the parent
		int parent = m_parentMap.get (nodeToBeRemovedStreamId); // Get parent
		
		System.out.println("Parent of " + nodeToBeRemovedStreamId + " is " + parent);
		List<Integer> tempvect = m_childrenMap.get (parent);	// Get parent's children
		tempvect.remove((Object) nodeToBeRemovedStreamId);				// Remove node from parent's children vect
		m_childrenMap.put(parent, tempvect);					// Update the children vect
		m_parentMap.remove (nodeToBeRemovedStreamId);			// Remove the node from the parent map
		
		// Now we need to handle the children of the node
		List<Integer>  childvect = m_childrenMap.get (nodeToBeRemovedStreamId);	// Get node's children
		int numOfKids = childvect.size ();
		
		
		for (int i = 0; i < childvect.size (); i++)
		{
			m_parentMap.remove(childvect.get(i));				// Remove each child from Parent map
		}
		
		// Remove all bucket entries of node which is leaving
		for (int i =0; i < MAX_CHILDREN - numOfKids; i++)
		{
			m_listOfBuckets.remove ((Object) nodeToBeRemovedStreamId);	// Remove all buckets from Parent map
		}
		
		// Now find the orphaned children some new parents
		for (int i = 0 ; i < childvect.size (); i++)
		{
			// For now, we do round robin, later, we might want to do a random join
			int newNodeStreamId = childvect.get (i);
			int bucket = m_listOfBuckets.get(0);
			m_parentMap.put(newNodeStreamId, bucket);
			List<Integer> tempvect1 =  m_childrenMap.get (bucket);
			tempvect1.add(newNodeStreamId);
			m_childrenMap.put (bucket, tempvect1);
			m_listOfBuckets.remove(0);
		}
		
		m_childrenMap.remove (nodeToBeRemovedStreamId);
	}
	
	public List<Integer> GetChildren (int queryingNodeStreamId)
	{
		return m_childrenMap.get (queryingNodeStreamId);
	}
	
	public int GetParent (int queryingNodeStreamId)
	{
		return m_parentMap.get(queryingNodeStreamId);
	}
}
