package eu.emdc.streamthing.message;

import peersim.core.Node;

public class VideoMessage extends StreamMessage {

	// video specifics here
	public int group_id;
	public int destStreamNodeId;
	
	public VideoMessage(int streamSourceId) {
		super(MessageType.VIDEO, streamSourceId);
	}

}
