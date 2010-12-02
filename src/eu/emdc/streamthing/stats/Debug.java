package eu.emdc.streamthing.stats;

import peersim.core.CommonState;

public class Debug {

	public static void info(String s) {
		System.out.println("[" + CommonState.getTime() + "] " + s);
	}
	
	public static void control(String s) {
		System.err.println("[" + CommonState.getTime() + "] " + s);
	}
}
