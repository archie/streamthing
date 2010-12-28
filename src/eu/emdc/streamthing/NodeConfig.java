package eu.emdc.streamthing;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.io.*;

public class NodeConfig {

	private static Map<Integer, Map<Integer, DelayTuple> > latencyMap = new HashMap<Integer, Map<Integer, DelayTuple> > ();
	private static Map<Integer, Float > uploadCapacityMap =  new HashMap <Integer, Float> ();

	private static int m_numNodes; // Being re-used. No consequences I guess - Lalith
	
	public static void InitialiseLatencyMap (String configFile){
		try
		{
			BufferedReader scanner = new BufferedReader(new FileReader(configFile));
			
			String firstline = scanner.readLine ();
			
			String [] fields = firstline.split(" +");
			
			m_numNodes = new Integer (fields[0]);
			
			int N = m_numNodes * (m_numNodes - 1); // Number of combinations
			
			firstline = null;
			fields = null;
			
			while (N > 0){
				
				

				String [] delayfields = scanner.readLine().split(" +");
							
				Integer nodeA = new Integer (delayfields[0]);
				Integer nodeB = new Integer (delayfields[1]);
				Integer minDelay = new Integer (delayfields[2]);
				Integer maxDelay = new Integer (delayfields[3]);
				
				DelayTuple delayTup = new DelayTuple();
				delayTup.SetMinDelay(minDelay);
				delayTup.SetMaxDelay(maxDelay);
				
				if (latencyMap.containsKey(nodeA))
				{
					latencyMap.get (nodeA).put (nodeB, delayTup);
				}
				else
				{
					Map< Integer, DelayTuple > innerMap = null;
					innerMap = new HashMap<Integer, DelayTuple> ();
					latencyMap.put(nodeA, innerMap);
				}
				
				N--;
			}
			
			scanner.close();
		}
		catch (IOException ioerr)
		{
			ioerr.printStackTrace();
		}
		
	}
	
	public static void InitialiseUploadCapacity (String configFile){
				
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
		catch (IOException ioerr)
		{
			ioerr.printStackTrace();
		}
	}
	
	public static int GetNNodes (){
		return m_numNodes;
	}
	
	public static DelayTuple GetDelayTupleForNodePair (int nodeA, int nodeB){
		
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
	
	public static Float GetUploadCapacityForNode (int nodeId){
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
