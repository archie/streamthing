package eu.emdc.streamthing.stats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Storing some statistical data for each node
 *
 */
public class MessageStatistics {

	public static Map<Integer, Integer> messageCountMap = new HashMap<Integer, Integer>();
	
	// latency
	public static Map<Integer, Long> latencyNodeMap = new HashMap<Integer, Long>();
	public static Map<Integer, Long> latencyStreamMap = new HashMap<Integer, Long>();
	
	// packet loss
	public static Map<Integer, Integer> droppedNodeMap = new HashMap<Integer, Integer>();
	public static Map<Integer, Integer> droppedStreamMap = new HashMap<Integer, Integer>();
	public static Map<Integer, Integer> unknownMap = new HashMap<Integer, Integer>();
	
	// upload capacity
	public static Map<Integer, Integer> peakUploadNodeMap = new HashMap<Integer, Integer>();
	public static Map<Integer, List<Integer>> bandwidthNodeMap = new HashMap<Integer, List<Integer>>();
	
	public static void latencyNode(int streamNodeId, long time) {
		if (messageCountMap.containsKey(streamNodeId)) 
		{
			int d = messageCountMap.get(streamNodeId);
			messageCountMap.put(streamNodeId, d+1);
			long oldLatency = latencyNodeMap.get(streamNodeId);
			latencyNodeMap.put(streamNodeId, oldLatency+time);
		} 
		else 
		{
			messageCountMap.put(streamNodeId, 1);
			latencyNodeMap.put(streamNodeId, time);
		}
	}
	
	public static void latencyStream(int streamId, long time) {
		if (latencyStreamMap.containsKey(streamId)) 
		{
			long oldLatency = latencyStreamMap.get(streamId);
			latencyStreamMap.put(streamId, oldLatency+time);
		} 
		else 
		{
			latencyStreamMap.put(streamId, time);
		}
	}
	
	public static void droppedNode(int streamNodeId) {
		if (droppedNodeMap.containsKey(streamNodeId)) 
		{
			int d = droppedNodeMap.get(streamNodeId);
			droppedNodeMap.put(streamNodeId, d+1);
		}
		else
		{
			droppedNodeMap.put(streamNodeId, 1);
		}
	}
	
	public static void droppedStream(int streamId) {
		if (droppedStreamMap.containsKey(streamId)) 
		{
			int d = droppedStreamMap.get(streamId);
			droppedStreamMap.put(streamId, d+1);
		}
		else
		{
			droppedStreamMap.put(streamId, 1);
		}
	}

	public static void unknown(int streamNodeId) {
		if (unknownMap.containsKey(streamNodeId)) 
		{
			int d = unknownMap.get(streamNodeId);
			unknownMap.put(streamNodeId, d+1);
		}
		else
		{
			unknownMap.put(streamNodeId, 1);
		}
	}
	
	public static void bandwidth(int streamNodeId, int bw) {
		if (peakUploadNodeMap.containsKey(streamNodeId)) {
			// if higher update peak
			if (bw > peakUploadNodeMap.get(streamNodeId))
				peakUploadNodeMap.put(streamNodeId, bw);
			
			bandwidthNodeMap.get(streamNodeId).add(bw);
			
		}
		else 
		{
			peakUploadNodeMap.put(streamNodeId, bw);
			bandwidthNodeMap.put(streamNodeId, new ArrayList<Integer>());
			bandwidthNodeMap.get(streamNodeId).add(bw);
		}
	}
	
}