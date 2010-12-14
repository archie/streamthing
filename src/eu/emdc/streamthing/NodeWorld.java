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
	
	public void RemoveNode (int nodeToBeRemovedStreamId)
	{
		System.err.println("Error: Not handling node removal case");
		/*
		int parent = m_parentMap.get (nodeToBeRemovedStreamId);
		List<Integer> tempvect = m_childrenMap.get (parent);
		tempvect.remove (nodeToBeRemovedStreamId);
		m_childrenMap.put(parent, tempvect);
		m_parentMap.remove (nodeToBeRemovedStreamId);
		
		// Need to handle it's children as well
		List<Integer>  childvect = m_childrenMap.get (nodeToBeRemovedStreamId);
		// Remove all bucket entries
		for (int i =0; i < m_listOfBuckets.size(); i++)
		{
			if (m_listOfBuckets.get(i) == nodeToBeRemovedStreamId)
			{
				
			}
		}*/
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
