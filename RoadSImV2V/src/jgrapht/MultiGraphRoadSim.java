package jgrapht;

import java.util.ArrayList;
import java.util.List;

public class MultiGraphRoadSim {
	
	private List<Node> nodes;
	private List<Edge> edges;
	
	public MultiGraphRoadSim(){
		this.nodes = new ArrayList<Node>();
		this.edges = new ArrayList<Edge>();
	}
	
	
	public void addNode(Node n){
		this.nodes.add(n);
	}
	
	public void addEdge(Edge e){
		this.edges.add(e);
	}
	
	public void addEdge(Node nodeI, Node nodeF, String id, double weight){
		Edge e = new Edge(nodeI, nodeF, id, weight);
		this.edges.add(e);
	}
	
	public Node getNodeById(String n){
		for(Node node:nodes){
			if(node.getId().compareTo(n) == 0){
				return node;
			}
		}
		return null;
	}
	
	public Edge getEdgeById(String n){
		for(Edge s:edges){
			if(s.getIdSegment().compareTo(n) == 0){
				return s;
			}
		}
		return null;
	}

	public List<Node> getNodes() {
		return nodes;
	}


	public void setNodes(List<Node> nodes) {
		this.nodes = nodes;
	}


	public List<Edge> getEdges() {
		return edges;
	}


	public void setEdges(List<Edge> edges) {
		this.edges = edges;
	}


	@Override
	public String toString() {
		return "MultiGraphRoadSim [nodes=" + nodes.toString() + ", edges=" + edges.toString() + "]";
	}
	
	

}
