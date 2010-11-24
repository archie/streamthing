package eu.emdc.streamthing;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.io.*;

public class NodeConfig {

	Map<Integer, Map<Integer, DelayTuple> > latencyMap;
	Map<Integer, Float > uploadCapacityMap;
	private int m_numNodes; // Being re-used. No consequences I guess - Lalith
	
	public void InitialiseLatencyMap (String configFile){
		
		latencyMap = new HashMap<Integer, Map<Integer, DelayTuple> > ();
		
		try
		{
			Scanner scanner = new Scanner (new File(configFile));
			
			if (scanner.hasNextInt())
			{
				m_numNodes = scanner.nextInt();
			}
			
			int N = m_numNodes * (m_numNodes - 1); // Number of combinations
			int minDelay;
			int maxDelay;
			int nodeA;
			int nodeB;
			while (N > 0){
				
				nodeA = scanner.nextInt ();
				nodeB = scanner.nextInt ();
				minDelay = scanner.nextInt ();
				maxDelay = scanner.nextInt ();
				Map< Integer, DelayTuple > innerMap = new HashMap<Integer, DelayTuple> ();
				DelayTuple delayTup = new DelayTuple();
				delayTup.SetMinDelay(minDelay);
				delayTup.SetMaxDelay(maxDelay);
				innerMap.put(nodeB, delayTup);
				
				latencyMap.put(nodeA, innerMap);
				
				N--;
			}
			
			scanner.close();
		}
		catch (Exception e)
		{
			System.err.println("Screw you guys, I'm going home.");
		}
		
	}
	
	public void InitialiseUploadCapacity (String configFile){
		
		uploadCapacityMap = new HashMap <Integer, Float> ();
		
		try
		{
			Scanner scanner = new Scanner (new File (configFile));
		
			if (scanner.hasNext()){
				m_numNodes = scanner.nextInt ();
			}
		
			int N = m_numNodes;
			int nodeId;
			float uploadCapacity;
			while (N > 0){
				// Read in (NodeId, UploadCapacity) tuples
				nodeId = scanner.nextInt();
				uploadCapacity = scanner.nextFloat();
				//uploadCapacityMap.put(scanner.nextInt(), scanner.nextFloat());
				System.out.println (nodeId + " " + uploadCapacity);
				uploadCapacityMap.put(nodeId, uploadCapacity);
				N--;
			}
		}
		catch (Exception e)
		{
			System.err.println("Error: InitialiseUploadCapacity");
		}
	}
	
	public int GetNNodes (){
		return m_numNodes;
	}
	
	public DelayTuple GetDelayTupleForNodePair (int nodeA, int nodeB){
		try
		{
			return latencyMap.get(nodeA).get(nodeB);
		}
		catch (Exception e)
		{
			System.err.println ("Error: GetDelayTupleForNodePair: invalid node pair");
			return null;
		}
	}
	
	public Float GetUploadCapacityForNode (int nodeId){
		try
		{
			return uploadCapacityMap.get(nodeId);
		}
		catch (Exception e)
		{
			System.err.println ("Error: GetUploadCapacityForNode: invalid node");
			return null;
		}
	}
	
}
