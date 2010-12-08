package eu.emdc.streamthing;

import java.io.File;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;
import java.util.Vector;


public class EventHelper {
	
	int m_numEvents;
	float m_endTime;
	
	Queue<StreamEvent> m_eventQueue = new PriorityQueue<StreamEvent>();
	
	public void InitialiseEvents (String eventFile){
		try
		{
			Scanner scanner = new Scanner (new File (eventFile));
			
			// Reading from event file. First read event header
			if (scanner.hasNext()){
				m_numEvents = scanner.nextInt();
			}
			
			if (scanner.hasNext()){
				m_endTime = scanner.nextFloat();
			}
				
			// From here on, we know there's a minimum of three fields.
			// Anything beyond that is appended into a vector.
			int N = m_numEvents;
					
			while (N > 0){
							
				StreamEvent newEvent = new StreamEvent ();
				newEvent.SetExecutionTime(scanner.nextFloat());
				newEvent.SetNodeId(scanner.nextInt());
				String et = scanner.next();
				
				Vector<Float> vect = new Vector<Float> ();
				
				if (et.equals("J")){
					newEvent.SetEventType (StreamEventType.JOIN);
				}
				else if (et.equals("L")){
					newEvent.SetEventType (StreamEventType.LEAVE);
					// Blank vect
				} 
				else if (et.equals("F")){
					newEvent.SetEventType (StreamEventType.FAIL);
					// Blank vect
				}
				else if (et.equals("P")){
					newEvent.SetEventType (StreamEventType.PUBLISH);
					vect.add (scanner.nextFloat()); // Stream ID
					vect.add (scanner.nextFloat()); // Stream duration
					vect.add (scanner.nextFloat()); // Stream rate
				}
				else if (et.equals("S")){
					newEvent.SetEventType (StreamEventType.SUBSCRIBE);
					vect.add (scanner.nextFloat()); // Stream ID
				}
				else if (et.equals("U")){
					newEvent.SetEventType (StreamEventType.UNSUBSCRIBE);
					vect.add (scanner.nextFloat()); // Stream ID
				}
				else if (et.equals("T")){
					float x;
					x = scanner.nextFloat();
					x = scanner.nextFloat();
					x = scanner.nextFloat();
					x = scanner.nextFloat();
				}
				
				if (!et.equals("T"))
				{
					newEvent.SetEventParams(vect);
				
					m_eventQueue.add (newEvent);
					N--;
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public Queue<StreamEvent> GetEventQueue (){
		return m_eventQueue;
	}
}
