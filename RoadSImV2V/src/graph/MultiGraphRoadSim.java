package graph;

import java.util.ArrayList;
import java.util.List;

public class MultiGraphRoadSim implements Cloneable {
	
	private List<Node> nodes;
	private List<Edge> edges;
	
	public MultiGraphRoadSim(){
		this.nodes = new ArrayList<Node>();
		this.edges = new ArrayList<Edge>();
	}

	/** Anyade un nuevo nodo a la lista*/
	public void addNode(Node n){
		this.nodes.add(n);
	}

	/** Anyade un nuevo Edge a la lista*/
	public void addEdge(Edge e){
		this.edges.add(e);
	}

	/** Método para consegur el Node a partir de un String (id)
	 * @param n id del nodo que se quiere conseguir
	 * @return El Node que encaje con el id o null */
	public Node getNodeById(String n){
		for(Node node:nodes){
			if(node.getId().compareTo(n) == 0){
				return node;
			}
		}
		return null;
	}

	/** Metodo para consegur el Edge a partir de un String (id)
	 * @param n id del segmento que se quiere conseguir
	 * @return El Edge que encage con el id o null */
	public Edge getEdgeById(String n){
		for(Edge s:edges){
			if(s.getIdSegment().compareTo(n) == 0){
				return s;
			}
		}
		return null;
	}

	/**Getters y Setters de los atributos de la clase*/

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
		return "MultiGraphRoadSim [nodes=" + nodes.toString() + 
				", edges=" + edges.toString() + "]";
	}

	/** Metodo para clonar el multigrafo*/
	@Override
	public Object clone() throws CloneNotSupportedException {
	    return super.clone();
	}
	
	

}
