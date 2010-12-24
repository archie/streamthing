package eu.emdc.streamthing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Vector;

public class EventHelper {

	int m_numEvents;
	int m_eventsReadSoFar = 0;
	long m_endTime;
	private BufferedReader reader;

	Queue<StreamEvent> m_eventQueue = new PriorityQueue<StreamEvent>();

	public void InitialiseEvents(String eventFile) {
		try {
			File file = new File(eventFile);
			reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(file)));

			// Reading from event file. First read event header
			String line = reader.readLine();
			String[] firstLine = line.split(" ");
			m_numEvents = Integer.parseInt(firstLine[0]);
			m_endTime = Long.parseLong(firstLine[1]);

			fillQueue(); // fill first time
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public StreamEvent poll() {
		fillQueue();

		return m_eventQueue.poll();
	}

	public long getNextTime() {
		StreamEvent event;

		fillQueue();

		if ((event = m_eventQueue.peek()) == null)
			return 0;
		else
			return (long) event.GetExecutionTime();
	}

	private boolean fillQueue() {
		if (m_eventQueue.size() > 0)
			return false;
		
		if (m_eventsReadSoFar == m_numEvents)
			return false;

		//System.out.println(" filling queue ");
		String line = null;
		StreamEvent newEvent;

		try {
			for (int i = 0; i < 1000; i++) { // read 1000 events at the time
				if ((line = reader.readLine()) == null) {
					System.out.println("finished, closing file");
					// reader.close();
					break;
				}

				m_eventsReadSoFar++;

				newEvent = parseLine(line.split(" "));
				if (newEvent != null) {
					m_eventQueue.add(newEvent);
				}
			}

		} catch (IOException e) {
			return false;
		}
		
		System.out.println(" Events read so far: " + m_eventsReadSoFar);
		return true;
	}

	private StreamEvent parseLine(String[] line) {
		StreamEvent event = new StreamEvent();
		event.SetExecutionTime(Float.parseFloat(line[0]));
		event.SetNodeId(Integer.parseInt(line[1]));
		event.SetEventType(getEventType(line[2]));
		if (line.length > 3) {
			event.SetEventParams(getEventParams(line));
		}
		return event;
	}

	private Vector<Float> getEventParams(String[] line) {
		Vector<Float> vect = new Vector<Float>();
		for (int i = 3; i < line.length; i++) {
			vect.add(Float.parseFloat(line[i]));
		}
		return vect;
	}

	private StreamEventType getEventType(String t) {
		if (t.equals("J"))
			return StreamEventType.JOIN;
		else if (t.equals("L"))
			return StreamEventType.LEAVE;
		else if (t.equals("F"))
			return StreamEventType.FAIL;
		else if (t.equals("U"))
			return StreamEventType.UNSUBSCRIBE;
		else if (t.equals("S"))
			return StreamEventType.SUBSCRIBE;
		else if (t.equals("P"))
			return StreamEventType.PUBLISH;
		else
			return StreamEventType.TURBULENCE;
	}

}
