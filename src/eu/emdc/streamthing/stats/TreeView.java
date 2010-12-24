package eu.emdc.streamthing.stats;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import eu.emdc.streamthing.NodeWorld;
import eu.emdc.streamthing.StreamThing;

import peersim.core.CommonState;
import peersim.core.Control;

public class TreeView implements Control {

	private static Map<Integer, String> m_streamIdToColor = new HashMap<Integer, String>();

	public TreeView(String prefix) {

	}

	public boolean execute() {
		String color;
		try {

			PrintWriter out = new PrintWriter(new File("graphs/all-"
					+ CommonState.getIntTime() + ".gv"));
			out.println("digraph unix { ");
			out.println("graph [	fontname = \"Verdana\"," +
							 "fontsize = 30," + 
							 "label = \"\\n\\n\\nStreamThing\\nPlotting " + StreamThing.m_videoStreamIdToMulticastTreeMap.size() + " streams\"];");	

			Iterator<Entry<Integer, NodeWorld>> it = StreamThing.m_videoStreamIdToMulticastTreeMap
					.entrySet().iterator();
			while (it.hasNext()) {

				Entry<Integer, NodeWorld> w = it.next();

				if (m_streamIdToColor.containsKey(w.getKey()))
					color = m_streamIdToColor.get(w.getKey());
				else {
					color = getColor();
					m_streamIdToColor.put(w.getKey(), color);
				}

				printChildren(out, w.getValue(), w.getValue().getRootNode(),
						getColor(), true);

			}
			out.println("}");
			out.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		
		 Iterator<Entry<Integer, NodeWorld>> it =
		 StreamThing.m_videoStreamIdToMulticastTreeMap.entrySet().iterator();
		 while (it.hasNext()) { Entry<Integer, NodeWorld> w = it.next();
		 
		 	printSeparateTree(w.getKey(), w.getValue());
		 
		 }
		 
		return false;
	}

	private void printSeparateTree(int streamId, NodeWorld w) {
		try {
			PrintWriter out = new PrintWriter(new File("graphs/" + streamId
					+ "-" + CommonState.getTime() + ".gv"));
			out.println("digraph unix { ");
			out.println("graph [	fontname = \"Verdana\"," +
					 "fontsize = 30," + 
					 "label = \"\\n\\n\\nStreamThing\\nPlotting stream " + streamId + " at " + CommonState.getTime() +  "\"];");
			printChildren(out, w, w.getRootNode(), true);
			out.println("}");
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void printChildren(PrintWriter out, NodeWorld w, int parent,
			String color, boolean first) {
		if (w.GetChildren(parent) != null && w.GetChildren(parent).size() == 0)
			return;
		else {
			if (first) {
				out.println("\"" + parent + "\" [color=\"" + color + "\", style=\"filled\"];");
			}
			if (w.GetChildren(parent) != null) {
				for (int child : w.GetChildren(parent)) {
					out.println("\"" + parent + "\" -> \"" + child
							+ "\" [color=\"" + color + "\"];");
					printChildren(out, w, child, color, false);
				}
			}
		}
	}

	private void printChildren(PrintWriter out, NodeWorld w, int parent, boolean first) {
		printChildren(out, w, parent, "black", false);
	}

	private String getColor() {
		int red = CommonState.r.nextInt(255);
		int green = CommonState.r.nextInt(255);
		int blue = CommonState.r.nextInt(255);

		Color color = new Color(red, green, blue);

		return "#" + Integer.toHexString(color.getRGB() & 0x00ffffff);

	}
}
