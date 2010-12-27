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
		System.out.println("Writing latency, packets, drops, jitter, bandwidth to file: " + logfileName);
		writeTransportLogToFile(logfileName);
		return true;
	}
	
	public void writeTransportLogToFile(String filename) {
		PrintWriter dataOutStream;
		
		try {
			dataOutStream = new PrintWriter(new File(filename));
			
			// packets 
			printPackets(dataOutStream);
			printDroppedPerStream(dataOutStream);
			
			// latency
			printLatency(dataOutStream);

			// jitter
			printJitter(dataOutStream);
			
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
			if (((NodeData)pairs.getValue()).packets > 0)
				dataOutStream.println("packets: " + pairs.getKey() + "\t" + ((NodeData)pairs.getValue()).packets);
		}
		Iterator<Entry<Integer, Integer>> drops = MessageStatistics.droppedNodeMap.entrySet().iterator();
		while (drops.hasNext()) {
			Entry<Integer, Integer> dropEntry = drops.next();
			if (dropEntry.getValue() > 0)
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
			if (latencyEntry.getKey() > 0)
				dataOutStream.println("latency-node: " + latencyEntry.getKey() + "\t" + 
					latencyEntry.getValue()/MessageStatistics.messageCountMap.get(latencyEntry.getKey()));
		}
		
		latencies = MessageStatistics.latencyStreamMap.entrySet().iterator();
		while (latencies.hasNext()) {
			Entry<Integer, Long> latencyEntry = latencies.next();
			
			if (latencyEntry.getKey() > 0)
				dataOutStream.println("latency-stream: " + latencyEntry.getKey() + "\t" + 
					latencyEntry.getValue()/MessageStatistics.streamMessageCountMap.get(latencyEntry.getKey()));
		}
		
	}
	
	private void printBandwidth(PrintWriter dataOutStream) {
		// peak node
		Iterator<Entry<Integer, Integer>> peaks = MessageStatistics.peakUploadNodeMap.entrySet().iterator();
		while (peaks.hasNext()) {
			Entry<Integer, Integer> peak = peaks.next();
			if (peak.getValue() > 0 && peak.getKey() > 0)
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
			if (average > 0 && avg.getKey() > 0)
				dataOutStream.println("avg-node: " + avg.getKey() + "\t" + average);
		}
	}
	
	private void printDroppedPerStream(PrintWriter dataOutStream) {
		
		Iterator<Entry<Integer, Integer>> drops = MessageStatistics.droppedStreamMap.entrySet().iterator();
		while (drops.hasNext()) {
			Entry<Integer, Integer> dropEntry = drops.next();
			if (dropEntry.getValue() > 0 && dropEntry.getKey() >= 0)
				dataOutStream.println("dropped-stream: " + dropEntry.getKey() + "\t" + dropEntry.getValue());
		}
		
	}
	
	// not used
	private void printJitter(PrintWriter dataOutStream) {
		Iterator<Entry<Integer, JitterTuple>> jitters;

		jitters = MessageStatistics.jitterNodeMap.entrySet().iterator();
		while (jitters.hasNext()) {
			Entry<Integer, JitterTuple> jitterEntry = jitters.next();
			dataOutStream.println("jitter-node: " + jitterEntry.getKey() + "\t" + jitterEntry.getValue().average);
		}
		
		// -----
		
		jitters = MessageStatistics.jitterStreamMap.entrySet().iterator();
		while (jitters.hasNext()) {
			Entry<Integer, JitterTuple> jitterEntry = jitters.next();
			dataOutStream.println("jitter-stream: " + jitterEntry.getKey() + "\t" + jitterEntry.getValue().average);
		}
		
	}

}
