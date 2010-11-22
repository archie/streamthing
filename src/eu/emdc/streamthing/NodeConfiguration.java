package eu.emdc.streamthing;

import java.util.HashMap;
import java.util.Map;

import peersim.core.Node;

public class NodeConfiguration {
	public static Map<Node, NodeConfig> load(String latency, String capacity) {
		Map<Node, NodeConfig> table = new HashMap<Node, NodeConfig>();
		
		System.out.println(latency + " " + capacity);
		
		return table;
	}
}
