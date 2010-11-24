package eu.emdc.streamthing;

public class DelayTuple {
	
	private float minDelay;
	private float maxDelay;
	
	public float GetMinDelay (){
		return minDelay;
	}
	
	public void SetMinDelay (float val){
		minDelay = val;
	}
	
	public float GetMaxDelay (){
		return maxDelay;
	}
	
	public void SetMaxDelay (float val){
		maxDelay = val;
	}
}
