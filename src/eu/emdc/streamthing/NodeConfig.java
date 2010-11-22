package eu.emdc.streamthing;

public class NodeConfig {
	private int latency = 0;
	private int uploadCapacity = 0;
	
	public int getLatency() {
		return latency;
	}
	public void setLatency(int latency) {
		this.latency = latency;
	}
	public int getUploadCapacity() {
		return uploadCapacity;
	}
	public void setUploadCapacity(int uploadCapacity) {
		this.uploadCapacity = uploadCapacity;
	}
	
	
}
