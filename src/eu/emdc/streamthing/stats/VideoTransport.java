package eu.emdc.streamthing.stats;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

import peersim.config.Configuration;
import peersim.core.Node;
import peersim.transport.Transport;

public class VideoTransport implements Transport {
	
	private static final String PAR_TRANSPORT = "transport";
	private final int transport;
	
	private PrintWriter dataOutStream;
	private Map<Long, Long> nodeData = new HashMap<Long, Long>();
	
	public VideoTransport(String prefix) {
		transport = Configuration.getPid(prefix+"."+PAR_TRANSPORT);
	}
	
	public Map<Long,Long> getNodesData() {
		return this.nodeData;
	}
	
	@Override
	public long getLatency(Node src, Node dest) {
		return 0;
	}

	@Override
	public void send(Node src, Node dest, Object msg, int pid) {
		if (nodeData.containsKey(src.getID())) {
			long current = nodeData.get(src.getID());
			nodeData.put(src.getID(), current + 1);
		} else {
			nodeData.put(src.getID(), 1l);
		}
			
		Transport t = (Transport) src.getProtocol(transport);
		t.send(src, dest, msg, pid);
	}

	
	@Override
	public Object clone() {
		return this;
	}

}
