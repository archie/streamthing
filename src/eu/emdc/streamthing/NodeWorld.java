package eu.emdc.streamthing;

import java.util.ArrayList;
import java.util.List;

import peersim.core.Node;

/**
 * Maintains the multicast tree(s)
 *
 */
public class NodeWorld {

	private List<Node> m_children; 
	private Node m_parent;
	private Node m_source;
	
	public NodeWorld() {
		m_children = new ArrayList<Node>();
	}
	
	public void pingChildren(Node src) {
		for (Node child : m_children) {
			/* or do this the other way around and keep a Map<Node, int> with last time pinged */
		}
	}
	
	public void setParent(Node node) {
		m_parent = node;
	}
	
	public Node getParent() {
		return m_parent;
	}
	
	public void setSource(Node node) {
		m_source = node;
	}
	
	public Node getSource() {
		return m_source;
	}
	
	public void addChild(Node node) {
		m_children.add(node);
	}
	
	public List<Node> getChildren() {
		return m_children;
	}
}
