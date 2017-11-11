package graph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiGraphRoadSim implements Serializable {

	private static final long serialVersionUID = 1L;
	private Map<String, Node> nodesMap;
	private Map<String, Edge> edgesMap;
	private List<Node> virtualNodes;
	
	public MultiGraphRoadSim(){
		this.nodesMap = new HashMap<String, Node>();
		this.edgesMap = new HashMap<String, Edge>();
		this.virtualNodes = new ArrayList<Node>();
	}

	/** Anyade un nuevo nodo a la lista*/
	public void addNode(Node n){
		nodesMap.put(n.getId(), n);
	}

	/** Anyade un nuevo Edge a la lista*/
	public void addEdge(Edge e){
		this.edgesMap.put(e.getIdSegment(), e);
		Node v = new Node()
		virtualNodes.add(new Node(e.getIdSegment()))
	}

	/** Metodo para conseguir el Node a partir de un String (id)
	 * @param n id del nodo que se quiere conseguir
	 * @return El Node que encaje con el id o null */
	public Node getNodeById(String n){
		return nodesMap.get(n);
	}

	/** Metodo para conseguir el Edge a partir de un String (id)
	 * @param n id del segmento que se quiere conseguir
	 * @return El Edge que encage con el id o null */
	public Edge getEdgeById(String n){
		return edgesMap.get(n);
	}

	public List<Edge> getEdges() {
		return new ArrayList<Edge>(edgesMap.values());
	}
	
	public List<Node> getNodes() {
		return new ArrayList<Node>(nodesMap.values());
	}

	@Override
	public String toString() {
		return "MultiGraphRoadSim [nodes=" + nodesMap.toString() + 
				", edges=" + edgesMap.toString() + "]";
	}
	
}
