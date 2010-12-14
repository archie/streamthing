package eu.emdc.streamthing;

import java.util.ArrayList;
import java.util.List;

import peersim.core.Node;

/**
 * Maintains the multicast tree(s)
 *
 */
public class NodeWorld {

	private List<Integer> m_children; 
	private Integer m_parent;
	private Integer m_source;
	
	public NodeWorld() {
		m_children = new ArrayList<Integer>();
	}
	
	public void pingChildren(Integer src) {
		for (Integer child : m_children) {
			/* or do this the other way around and keep a Map<Node, int> with last time pinged */
		}
	}
	
	public void setParent(Integer node) {
		m_parent = node;
	}
	
	public Integer getParent() {
		return m_parent;
	}
	
	public void setSource(Integer node) {
		m_source = node;
	}
	
	public Integer getSource() {
		return m_source;
	}
	
	public void addChild(Integer node) {
		m_children.add(node);
	}
	
	public List<Integer> getChildren() {
		return m_children;
	}
}
