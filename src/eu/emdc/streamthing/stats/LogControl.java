package eu.emdc.streamthing.stats;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;

import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

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
		System.out.println("Network size: " + Network.size());
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
				dataOutStream.print(pairs.getValue() + "\t" + ((Node)pairs.getKey()).getID());
				dataOutStream.println();
			}
			
			dataOutStream.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} 
	}

}
