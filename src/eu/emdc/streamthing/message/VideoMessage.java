package eu.emdc.streamthing.message;

import peersim.core.Node;

public class VideoMessage extends StreamMessage {

	// video specifics here
	public int stream_id;
	public int group_id;
	
	public VideoMessage(Node source) {
		super(MessageType.VIDEO, source);
	}

}
