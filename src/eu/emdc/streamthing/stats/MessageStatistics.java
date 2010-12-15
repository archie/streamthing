package eu.emdc.streamthing.stats;

import java.util.HashMap;
import java.util.Map;

public class MessageStatistics {

	private static Map<Integer, Integer> messageCountMap = new HashMap<Integer, Integer>();
	private static Map<Integer, Long> latencyMap = new HashMap<Integer, Long>();
	private static Map<Integer, Integer> droppedMap = new HashMap<Integer, Integer>();
	
	public static void latency(int streamNodeId, long time) {
		if (messageCountMap.containsKey(streamNodeId)) 
		{
			int d = messageCountMap.get(streamNodeId);
			messageCountMap.put(streamNodeId, d++);
			long oldLatency = latencyMap.get(streamNodeId);
			latencyMap.put(streamNodeId, oldLatency+time);
		} 
		else 
		{
			messageCountMap.put(streamNodeId, 1);
			latencyMap.put(streamNodeId, time);
		}
	}
	
	public static void dropped(int streamNodeId) {
		if (droppedMap.containsKey(streamNodeId)) 
		{
			int d = droppedMap.get(streamNodeId);
			droppedMap.put(streamNodeId, d++);
		}
		else
		{
			droppedMap.put(streamNodeId, 1);
		}
	}

	
}
