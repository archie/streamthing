package eu.emdc.streamthing.stats;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Storing some statistical data for each node
 *
 */
public class MessageStatistics {

	public static Map<Integer, Integer> messageCountMap = new HashMap<Integer, Integer>();
	public static Map<Integer, Integer> streamMessageCountMap = new HashMap<Integer, Integer>();
	
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
	
	// avg jitter
	public static Map<Integer, JitterTuple> jitterNodeMap = new HashMap<Integer, JitterTuple>();
	public static Map<Integer, JitterTuple> jitterStreamMap = new HashMap<Integer, JitterTuple>();
	
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
			int d = streamMessageCountMap.get(streamId);
			streamMessageCountMap.put(streamId, d+1);
			
			long oldLatency = latencyStreamMap.get(streamId);
			latencyStreamMap.put(streamId, oldLatency+time);
		} 
		else 
		{
			streamMessageCountMap.put(streamId, 1);
			latencyStreamMap.put(streamId, time);
		}
	}
	
	public static void jitterNode(int streamNodeId, long time) {
		// J(N) = (J(N-1) * (N - 1) + LatestDifference) / N
		long latestDifference;
		JitterTuple jt = new JitterTuple();
		if (jitterNodeMap.containsKey(streamNodeId)) {
			JitterTuple oldJt = jitterNodeMap.get(streamNodeId);
			
			if (oldJt.sampleNumber >= 2) 
			{
				latestDifference = Math.abs(oldJt.lastTime - time);
				jt.sampleNumber = oldJt.sampleNumber + 1;
				jt.lastJitter = (oldJt.lastJitter * (jt.sampleNumber-1) + latestDifference) / jt.sampleNumber;
				jt.lastTime = time;
			} 
			else 
			{
				if (oldJt.sampleNumber == 1) {
					//J1 = (X + Y)/2
					latestDifference = Math.abs(jt.lastTime - time);
					jt.lastJitter = (jt.intermediary + latestDifference) / 2;
					jt.lastTime = time;
					jt.sampleNumber = 2;
				} else {
					latestDifference = Math.abs(oldJt.lastTime - time);
					jt.lastTime = time;
					jt.intermediary = latestDifference;
					jt.sampleNumber = 1;
				}
			}
			jitterNodeMap.put(streamNodeId, jt);
		} else {
			jt.lastTime = time;
			jt.lastJitter = -1;
			jt.sampleNumber = 0;
			jt.intermediary = -1;
			jitterNodeMap.put(streamNodeId, jt);
		}
	}
	
	public static void jitterStream(int streamId, long time) {
		long latestDifference;
		JitterTuple jt = new JitterTuple();
		if (jitterStreamMap.containsKey(streamId)) {
			JitterTuple oldJt = jitterStreamMap.get(streamId);
			
			if (oldJt.sampleNumber >= 2) 
			{
				latestDifference = Math.abs(oldJt.lastTime - time);
				jt.sampleNumber = oldJt.sampleNumber + 1;
				jt.lastJitter = (oldJt.lastJitter * (jt.sampleNumber-1) + latestDifference) / jt.sampleNumber;
				jt.lastTime = time;
			} 
			else 
			{
				if (oldJt.sampleNumber == 1) {
					//J1 = (X + Y)/2
					latestDifference = Math.abs(jt.lastTime - time);
					jt.lastJitter = (jt.intermediary + latestDifference) / 2;
					jt.lastTime = time;
					jt.sampleNumber = 2;
				} else {
					latestDifference = Math.abs(oldJt.lastTime - time);
					jt.lastTime = time;
					jt.intermediary = latestDifference;
					jt.sampleNumber = 1;
				}
			}
			jitterStreamMap.put(streamId, jt);
		} else {
			jt.lastTime = time;
			jt.lastJitter = -1;
			jt.sampleNumber = 0;
			jt.intermediary = -1;
			jitterStreamMap.put(streamId, jt);
		}
	}
	
	public static void logLatencyJitter(int streamId, int streamNodeId, long latency, long currentTime) {
		latencyNode(streamNodeId, latency);
		latencyStream(streamId, latency);
		jitterNode(streamNodeId, currentTime);
		jitterStream(streamId, currentTime);
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
