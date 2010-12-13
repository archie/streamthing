package eu.emdc.streamthing.stats;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import eu.emdc.streamthing.message.StreamMessage;

import peersim.config.Configuration;
import peersim.core.CommonState;
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
		return 0;
	}

	@Override
	public void send(Node src, Node dest, Object msg, int pid) {
		if (nodeData.containsKey(src.getID())) {
			NodeData data = nodeData.get(src.getID());
			data.packets++;
			data.latency += latency(msg);
			nodeData.put(src.getID(), data);
		} else {
			NodeData data = new NodeData();
			data.packets = 1l;
			data.latency = latency(msg);
			nodeData.put(src.getID(), data);
		}
			
		Transport t = (Transport) src.getProtocol(transport);
		t.send(src, dest, msg, pid);
	}
	
	private long latency(Object msg) {
		if (msg instanceof StreamMessage) {
			System.out.println("latency: " + (CommonState.getTime() - ((StreamMessage)msg).sent));
			return CommonState.getTime() - ((StreamMessage)msg).sent;
		}
		return 0;
	}

	
	@Override
	public Object clone() {
		return this;
	}

}
