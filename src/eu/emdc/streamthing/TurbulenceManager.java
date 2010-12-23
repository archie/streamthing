package eu.emdc.streamthing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
		public int id;
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
	
	public int getTurbulenceBandwidth() {
		int sum = 0; 
		
		Iterator<Entry<Integer, TurbulenceData>> it = m_turbulence.entrySet().iterator();
		while (it.hasNext()) {
			Entry<Integer, TurbulenceData> e = it.next();
			sum += e.getValue().rate;// * e.getValue().size;
		}
				
		return sum;
	}

	public void startTurbulence(Node src, int streamDestId, int duration,
			int rate, int size, int pid) {
		StreamThing.m_turbulenceCount++;

		TurbulenceData data = new TurbulenceData();
		data.streamDestId = streamDestId;
		data.duration = duration;
		data.rate = rate;
		data.size = size;
		data.started = CommonState.getTime();
		data.id = StreamThing.m_turbulenceCount;

		m_turbulence.put(data.id, data);

		TurbulenceEvent event = new TurbulenceEvent();
		event.turbulenceId = data.id;
		
		sendTurbulence(src, event, pid);
	}

	public void sendTurbulence(Node src, TurbulenceEvent event, int pid) {
		//System.out.println(event.turbulenceId + " src: " + StreamThing.GetStreamIdFromNodeId(src.getID()) + " to " + m_turbulence.get(event.turbulenceId).streamDestId);
		if (m_turbulence.containsKey(event.turbulenceId)) {
			TurbulenceData data = m_turbulence.get(event.turbulenceId);

			VideoMessage msg = new VideoMessage(StreamThing.GetStreamIdFromNodeId(src.getID()));
			msg.destStreamNodeId = data.streamDestId;
			msg.streamId = -1;

			m_transport.addToQueue(src, msg, data.size);

			if ((CommonState.getTime() + 1000 / data.rate) < (data.started + data.duration)) {
				EDSimulator.add(1000 / data.rate, event, src, pid);
			} else {
				m_turbulence.remove(event.turbulenceId);
			}
		}

		if (m_transport.getQueueSize() == 1) {
			EDSimulator.add(1000 / m_uploadCapacity, new VideoTransportEvent(),
					src, pid);
		}
	}
}
