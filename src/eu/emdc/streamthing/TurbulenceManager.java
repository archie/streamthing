package eu.emdc.streamthing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import peersim.core.CommonState;
import peersim.core.Node;
import peersim.edsim.EDSimulator;
import eu.emdc.streamthing.message.VideoMessage;
import eu.emdc.streamthing.transport.TransportControl;

public class TurbulenceManager {

	private TransportControl m_transport;
	private int m_uploadCapacity; 
	private Map<Integer, TurbulenceData> m_turbulence = new HashMap<Integer, TurbulenceData>();
	
	class TurbulenceData {
		public int streamDestId;
		public int duration; 
		public int rate;
		public int size;
		public long started;
	}
	
	public TurbulenceManager(TransportControl transport, int upcapacity) {
		m_uploadCapacity = upcapacity;
		m_transport = transport;
	}
	
	public void startTurbulence(Node src, int streamDestId, int duration, int rate, int size, int pid) {
		TurbulenceData data = new TurbulenceData();
		data.streamDestId = streamDestId;
		data.duration = duration;
		data.rate = rate;
		data.size = size;
		data.started = CommonState.getTime();
		m_turbulence.put(streamDestId, data);
		
		TurbulenceEvent event = new TurbulenceEvent();
		event.streamDestId = streamDestId;
		sendTurbulence(src, event, pid);
	}
	
	public void sendTurbulence(Node src, TurbulenceEvent event, int pid) {
		TurbulenceData data = m_turbulence.get(event.streamDestId);
		
		VideoMessage msg = new VideoMessage(src);
		msg.streamId = -1;
		
		m_transport.addToQueue(src, msg, data.size);
		
		if ((CommonState.getTime() + 1000/data.rate) < (data.started + data.duration)) {
			EDSimulator.add(1000/data.rate, event, src, pid);
		}
		
		if (m_transport.getQueueSize() == 1) 
		{
			EDSimulator.add(1000/m_uploadCapacity, new VideoTransportEvent(), src, pid);
		}
	}
}
