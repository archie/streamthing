package eu.emdc.streamthing.stats;

import java.io.PrintWriter;
import java.util.Map;
import java.util.HashMap;

import peersim.config.Configuration;
import peersim.core.Node;
import peersim.transport.Transport;

public class VideoTransport implements Transport {
	
	private static final String PAR_TRANSPORT = "transport";
	private final int transport;
	
	private PrintWriter dataOutStream;
	public class NodeData {
		public long packets;
		public long latency;
	}
	private Map<Long, NodeData> nodeData = new HashMap<Long, NodeData>();
	
	public VideoTransport(String prefix) {
		transport = Configuration.getPid(prefix+"."+PAR_TRANSPORT);
	}
	
	public Map<Long,NodeData> getNodesData() {
		return this.nodeData;
	}
	
	@Override
	public long getLatency(Node src, Node dest) {
		Transport t = (Transport) src.getProtocol(transport);
		return t.getLatency(src, dest);
	}

	@Override
	public void send(Node src, Node dest, Object msg, int pid) {
		if (nodeData.containsKey(src.getID())) {
			NodeData data = nodeData.get(src.getID());
			data.packets++;
			nodeData.put(src.getID(), data);
		} else {
			NodeData data = new NodeData();
			data.packets = 1l;
			data.latency = 0;
			nodeData.put(src.getID(), data);
		}
			
		Transport t = (Transport) src.getProtocol(transport);
		t.send(src, dest, msg, pid);
	}

	
	@Override
	public Object clone() {
		return this;
	}

}
