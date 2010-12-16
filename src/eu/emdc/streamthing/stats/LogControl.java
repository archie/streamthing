package eu.emdc.streamthing.stats;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import eu.emdc.streamthing.stats.VideoTransport.NodeData;

import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;

public class LogControl implements Control {

	private static final String PAR_TRANSPORT = "transport";
	private VideoTransport accountingProtocol;
	private String logfileName;
	
	public LogControl(String prefix) {
		this.accountingProtocol =(VideoTransport) Network.get(0).getProtocol(Configuration.getPid(prefix+"."+PAR_TRANSPORT));
		this.logfileName = Configuration.getString(prefix + "." + "logfile");
	}
	
	@Override
	public boolean execute() {
		writeTransportLogToFile(logfileName);
		System.err.println("Network size: " + Network.size());
		return true;
	}
	
	public void writeTransportLogToFile(String filename) {
		System.err.println("LogControl: writing " + accountingProtocol.getNodesData().size() + " items to file");
		PrintWriter dataOutStream;
		
		try {
			dataOutStream = new PrintWriter(new File(filename));
			Iterator it = accountingProtocol.getNodesData().entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry pairs = (Map.Entry)it.next();
				dataOutStream.print("packets: " + ((NodeData)pairs.getValue()).packets + "\t" + pairs.getKey());
				dataOutStream.println();
			}
			
			Iterator<Entry<Integer, Long>> latencies = MessageStatistics.latencyMap.entrySet().iterator();
			while (latencies.hasNext()) {
				Entry<Integer, Long> latencyEntry = latencies.next();
				dataOutStream.println("latency: " + latencyEntry.getKey() + "\t" + latencyEntry.getValue());
			}
			
			Iterator<Entry<Integer, Integer>> drops = MessageStatistics.droppedMap.entrySet().iterator();
			while (drops.hasNext()) {
				Entry<Integer, Integer> dropEntry = drops.next();
				dataOutStream.println("dropped: " + dropEntry.getKey() + "\t" + dropEntry.getValue());
			}
			
			Iterator<Entry<Integer, Integer>> unknown = MessageStatistics.unknownMap.entrySet().iterator();
			while (unknown.hasNext()) {
				Entry<Integer, Integer> unknownEntry = unknown.next();
				dataOutStream.println("unknown: " + unknownEntry.getKey() + "\t" + unknownEntry.getValue());
			}
			
			dataOutStream.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} 
	}

}
