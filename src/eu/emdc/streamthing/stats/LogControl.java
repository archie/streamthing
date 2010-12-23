package eu.emdc.streamthing.stats;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
			
			// packets 
			printPackets(dataOutStream);
			
			// latency
			printLatency(dataOutStream);

			// jitter
			//printJitter(dataOutStream);
			
			// bandwidth
			printBandwidth(dataOutStream);
			
			
			dataOutStream.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} 
	}
	
	private void printPackets(PrintWriter dataOutStream) {
		Iterator it = accountingProtocol.getNodesData().entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry pairs = (Map.Entry)it.next();
			dataOutStream.print("packets: " + pairs.getKey() + "\t" + ((NodeData)pairs.getValue()).packets);
			dataOutStream.println();
		}
		Iterator<Entry<Integer, Integer>> drops = MessageStatistics.droppedNodeMap.entrySet().iterator();
		while (drops.hasNext()) {
			Entry<Integer, Integer> dropEntry = drops.next();
			dataOutStream.println("dropped: " + dropEntry.getKey() + "\t" + dropEntry.getValue());
		}
		// hack to make graphing easier
		if (MessageStatistics.droppedNodeMap.size() == 0)
			dataOutStream.println("dropped: 0\t0");
	}
	
	private void printLatency(PrintWriter dataOutStream) {
		Iterator<Entry<Integer, Long>> latencies;
		latencies = MessageStatistics.latencyNodeMap.entrySet().iterator();
		while (latencies.hasNext()) {
			Entry<Integer, Long> latencyEntry = latencies.next();
			dataOutStream.println("latency-node: " + latencyEntry.getKey() + "\t" + 
					latencyEntry.getValue()/MessageStatistics.messageCountMap.get(latencyEntry.getKey()));
		}
		
		latencies = MessageStatistics.latencyStreamMap.entrySet().iterator();
		while (latencies.hasNext()) {
			Entry<Integer, Long> latencyEntry = latencies.next();
			System.out.println(latencyEntry.getValue() + " " + MessageStatistics.streamMessageCountMap.get(latencyEntry.getKey()));
			
			dataOutStream.println("latency-stream: " + latencyEntry.getKey() + "\t" + 
					latencyEntry.getValue()/MessageStatistics.streamMessageCountMap.get(latencyEntry.getKey()));
		}
		
	}
	
	private void printBandwidth(PrintWriter dataOutStream) {
		// peak node
		Iterator<Entry<Integer, Integer>> peaks = MessageStatistics.peakUploadNodeMap.entrySet().iterator();
		while (peaks.hasNext()) {
			Entry<Integer, Integer> peak = peaks.next();
			if (peak.getValue() > 0)
				dataOutStream.println("peak-node: " + peak.getKey() + "\t" + peak.getValue());
		}
		
		// avg upload
		Iterator<Entry<Integer, List<Integer>>> avgs = MessageStatistics.bandwidthNodeMap.entrySet().iterator();
		while (avgs.hasNext()) {
			Entry<Integer, List<Integer>> avg = avgs.next();
			
			int total = 0;
			for (int i : avg.getValue())
				total += i;
			
			int average = total / avg.getValue().size();
			if (average > 0)
				dataOutStream.println("avg-node: " + avg.getKey() + "\t" + average);
		}
	}
	
	private void printJitter(PrintWriter dataOutStream) {
		int network_node_mean = 0;
		int network_stream_mean = 0;
		Iterator<Entry<Integer, Long>> latencies;
		//List<Double> jitter = new ArrayList<Double>();
		
		latencies = MessageStatistics.latencyNodeMap.entrySet().iterator();
		while (latencies.hasNext()) {
			Entry<Integer, Long> latencyEntry = latencies.next();
			network_node_mean += latencyEntry.getValue();
		}
		
		network_node_mean = network_node_mean/MessageStatistics.latencyNodeMap.size();
		
		latencies = MessageStatistics.latencyNodeMap.entrySet().iterator();
		while (latencies.hasNext()) {
			Entry<Integer, Long> latencyEntry = latencies.next();
			dataOutStream.println("jitter-node: " + latencyEntry.getKey() + "\t" + Math.abs(latencyEntry.getValue()-network_node_mean));
		}
		
		// -----
		
		latencies = MessageStatistics.latencyStreamMap.entrySet().iterator();
		while (latencies.hasNext()) {
			Entry<Integer, Long> latencyEntry = latencies.next();
			network_stream_mean += latencyEntry.getValue();
		}
		
		network_stream_mean = network_stream_mean/MessageStatistics.latencyStreamMap.size();
		
		latencies = MessageStatistics.latencyStreamMap.entrySet().iterator();
		while (latencies.hasNext()) {
			Entry<Integer, Long> latencyEntry = latencies.next();
			dataOutStream.println("jitter-stream: " + latencyEntry.getKey() + "\t" + Math.abs(latencyEntry.getValue()-network_stream_mean));
		}
		
	}

}
