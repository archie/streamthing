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
	private Map<Node, Long> nodeData = new HashMap<Node, Long>();
	
	public VideoTransport(String prefix) {
		transport = Configuration.getPid(prefix+"."+PAR_TRANSPORT);
	}
	
	public Map<Node,Long> getNodesData() {
		return this.nodeData;
	}
	
	@Override
	public long getLatency(Node src, Node dest) {
		return 0;
	}

	@Override
	public void send(Node src, Node dest, Object msg, int pid) {
		if (nodeData.containsKey(src)) {
			long current = nodeData.get(src);
			nodeData.put(src, current + 1);
		} else 
			nodeData.put(src, 1l);

		Transport t = (Transport) src.getProtocol(transport);
		t.send(src, dest, msg, pid);
	}
	
	public void writeTransportLogToFile(String filename) {
		System.out.println("writing to file");

		try {
			dataOutStream = new PrintWriter(new File(filename));
			Iterator it = nodeData.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pairs = (Map.Entry)it.next();
				dataOutStream.print(pairs.getValue() + "\t" + ((Node)pairs.getKey()).getID());
				dataOutStream.println();
			}
			
			dataOutStream.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} 
	}

	
	@Override
	public Object clone() {
		return this;
	}

}
