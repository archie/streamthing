package eu.emdc.streamthing.stats;

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
		this.accountingProtocol.writeTransportLogToFile(logfileName);
		return true;
	}

}
