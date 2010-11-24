package eu.emdc.streamthing;

import java.util.Vector;

public class StreamEvent implements Comparable<StreamEvent>{
		
	private float m_execTime;
	private int m_nodeId;
	private StreamEventType m_type;
	
	private Vector<Float> m_paramVector = new Vector<Float> ();
	
	public float GetExecutionTime (){
		return m_execTime;
	}
	
	public void SetExecutionTime (float time){
		m_execTime = time;
	}
	
	public int GetNodeId (){
		return m_nodeId;
	}
	
	public void SetNodeId (int id){
		m_nodeId = id;
	} 

	public StreamEventType GetEventType (){
		return m_type;
	}
	
	public void SetEventType (StreamEventType et){
		m_type = et;
	}
	
	public Vector<Float> GetEventParams (){
		return m_paramVector;
	}
	
	public void SetEventParams (Vector<Float> vect){
		m_paramVector = vect;
	}
	
	public void SetEvent (float time, int id, StreamEventType et, Vector<Float> vect){
		m_execTime = time;
		m_nodeId = id;
		m_type = et;
		m_paramVector = vect;
	}

	@Override
	public int compareTo(StreamEvent o) {
		// TODO Auto-generated method stub
		if (this.GetExecutionTime() > o.GetExecutionTime())
			return 1;
		else if (this.GetExecutionTime() == o.GetExecutionTime())
			return 0;
		else
			return -1;
	}
}
