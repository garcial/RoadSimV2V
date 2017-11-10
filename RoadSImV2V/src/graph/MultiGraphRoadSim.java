package graph;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MultiGraphRoadSim implements Serializable {

	private static final long serialVersionUID = 1L;
	private Map<String, Node> nodesMap;
	private Map<String, Edge> edgesMap;
	
	public MultiGraphRoadSim(){
		this.nodesMap = new HashMap<String, Node>();
		this.edgesMap = new HashMap<String, Edge>();
	}

	/** Anyade un nuevo nodo a la lista*/
	public void addNode(Node n){
		nodesMap.put(n.getId(), n);
	}

	/** Anyade un nuevo Edge a la lista*/
	public void addEdge(Edge e){
		this.edgesMap.put(e.getIdSegment(), e);
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

	public Collection<Edge> getEdges() {
		return edgesMap.values();
	}
	
	public Collection<Node> getNodes() {
		return nodesMap.values();
	}

	@Override
	public String toString() {
		return "MultiGraphRoadSim [nodes=" + nodesMap.toString() + 
				", edges=" + edgesMap.toString() + "]";
	}
	
}
