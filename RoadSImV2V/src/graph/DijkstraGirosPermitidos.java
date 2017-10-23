package graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DijkstraGirosPermitidos {
	
	/**
	 * El algoritmo se basa en el capítulo 3 de Inteligencia Artificial para
	 * desarrolladores. Conceptos e implementación en C#
	 * */

	/** Parámetros del dijkstra
	 *
	 * nodes - Lista de nodos sobre los que se ejecuta el dijkstra
	 *
	 * unsettleNodes - Lista de los nodos no visitados
	 *
	 * predecessors - La clave es el nodo destino y el valor es el nodo origen
	 * 		(al nodo destino llegas a partir del nodo origen)
	 *
	 * distance - La key de las distancias es ( id de la intersección ¿ id del
 	 *			segmento), ejemplo:(I-A1¿S-A1). El segmento que se utiliza en
	 *			las distancias es el segmento por el cual llegas a esa
	 *			intersección. En el caso de que sea el primer nodo solo se pone
	 *			el nombre de la	intersección ya que no tienes un
	 *			segmento de entrada.
	 *
	 * source - Nodo de origen del algoritmo
	 *
	 * target - Nodo de destino del algoritmo
	 *
	 * */
	private List<Node> nodes;
	private Set<Node> unSettledNodes;
	private Map<Node, Node> predecessors;
	private Map<Node, Double> distance;
	private Node source;
	private Node target;

	/** Constructor de Dijkstra
	 * @param graph - Grafo sobre el que se ejecuta Dijkstra
	 * */
	public DijkstraGirosPermitidos(MultiGraphRoadSim graph) {
		this.nodes = graph.getNodes();
		this.unSettledNodes = new HashSet<Node>();
		this.distance = new HashMap<Node, Double>();
		this.predecessors = new HashMap<Node, Node>();
		for(Edge i : graph.getEdges()){
			Node inter = new Node(
					i.getDestination().getId() + "¿" + i.getIdSegment());
			this.unSettledNodes.add(inter);
			this.distance.put(inter, Double.MAX_VALUE);
			this.predecessors.put(inter, null);
		}

	}

	/**
	 * Método de ejecución principal del dijkstra para llegar
	 * del origen al destino de la manera más rápida.
	 * @param source - Nodo origen
	 * @param target - Nodo destino
	 * */
	public void execute(Node source, Node target) {
		this.source = source;
		this.target = target;
		/* Se añade el nodo origen a los no visitado
		Este nodo no incorpora el camino por el que
		llegas ya que inicias el camino desde esa intersección*/
		unSettledNodes.add(source);
		distance.put(source, 0.0);
		while (unSettledNodes.size() > 0) {
			Node node = getMinimum(unSettledNodes);
			findMinimalDistances(node);
			if(node.getId().compareTo(
					target.getId().split("¿")[0]) == 0)
				break;
		}
	}

	/**
	 * Este método busca el nodo con menor peso de los no recorridos
	 * @param Nodes lista de nodos
	 * @return Node con menor peso
	 * */
	private Node getMinimum(Set<Node> Nodes) {
		Node minimum = null;
		for (Node Node : Nodes) {
			if (minimum == null) {
				minimum = Node;
			} else {
				if (getShortestDistance(Node) < getShortestDistance(minimum)) {
					minimum = Node;
				}
			}
		}
		return minimum;
	}

	/**
	 * En el caso de tener la distancia registrada no la vuelve a calcular y
	 * la coge del mapa sino la tiene registrada le asigna infinito
	 * @param destination es el nodo del que se quiere conseguir la distancia
	 * @return Distancia del nodo destination
	 * */
	private double getShortestDistance(Node destination) {
		return distance.get(destination);
	}

	/**
	 * Este método busca el camino con menor peso
	 * @param node es el nodo origen del que se calcula
	 *             la distancia de los vecinos
	 * */
	private void findMinimalDistances(Node node) {
		//Coge los nodos vecinos
		List<Node> adjacentNodes = getNeighbors(node);
		for (Node target : adjacentNodes) {
			//Coge el camino más corto del nodo
			double shortestNode = getShortestDistance(node);
			//Coge el camino más corto del vecino
			double shortestTarget = getShortestDistance(target);
			//Distancia del nodo al vecino
			double distandeNodeTarget = getDistance(node,target);
			if (shortestTarget > shortestNode + distandeNodeTarget) {
				distance.put(target, shortestNode + distandeNodeTarget);
				predecessors.put(target, node);
			}
		}
		unSettledNodes.remove(node);
	}

	/**
	 * Consigue los vecinos de un nodo específico
	 * @param node Nodo del que se quiere averiguar los vecinos
	 * @return Lista de nodos vecinos
	 * */
	private List<Node> getNeighbors(Node node) {
		List<Node> neighbors = new ArrayList<Node>();
		List<Edge> candidates = new ArrayList<Edge>();
		// Si es el nodo origen cogemos como candidatos
		// los segmentos de salida, sino cogeremos los segmentos permitidos
		if(node.getId().compareTo(this.source.getId()) == 0){
			candidates = node.getSegmentOut();
		}else{
			Node interseccion = null;
			for (Node nodeTest: this.nodes){
				if(nodeTest.getId().compareTo(
						node.getId().split("¿")[0]) == 0){
					interseccion = nodeTest;
				}
			}
			List<Edge> listSegments = interseccion.getAllowedSegments(
					interseccion.getSegmentById(node.getId().split("¿")[1]));
			for(Edge s: listSegments){
					candidates.add(s);
			}
		}
		//Los candidatos son los segmentos por los que
		// @param node llega a los vecinos
		for (Edge edge : candidates) {
				Iterator<Node> iter = this.unSettledNodes.iterator();
				while(iter.hasNext()){
					Node cand = (Node) iter.next();
					if(cand.getId().compareTo(
							edge.getDestination().getId() + "¿" +
									edge.getIdSegment()) == 0){
						neighbors.add(cand);
					}
				}
		}

		return neighbors;
	}

	/**
	 * Calcula la distancia entre un nodo origen y un nodo destino
	 * @param node Nodo del que partimos para calcular la distancia
	 * @param target Nodo al que llegamos
	 * @return Double de la distancia de node a target
	 * */
	private double getDistance(Node node, Node target) {
		double minDistance = distance.get(target);
		// neighborsSegments es una lista de segmentos por el
		// que llegan de node a target
		ArrayList<Edge> neighborsSegments = new ArrayList<Edge>();
		// En el caso del primer nodo solo tiene la id de la intersección
		String[] nodeParts = node.getId().split("¿");
		String[] targetParts = target.getId().split("¿");

		// The parts of the target are [intersectionDestination,
		// segmentFromOriginToDestination]
		if(nodeParts[0].compareTo(this.source.getId()) == 0){
			neighborsSegments = (ArrayList<Edge>) node.getSegmentOut();
		} else {
			Node nodePrimary = null;
			for(Node nodeTest : this.nodes){
				if(nodeTest.getId().compareTo(nodeParts[0]) == 0){
					nodePrimary = nodeTest;
					break;
				}
			}
			neighborsSegments.addAll(nodePrimary.getAllowedSegments(
					nodePrimary.getSegmentById(nodeParts[1])));
		}

		// Comprueba que es un segmento permitido y si el peso es el mínimo
		for (Edge edge : neighborsSegments) {
			if (edge.getIdSegment().equals(targetParts[1]) &&
					edge.getDestination().getId().equals(targetParts[0]) &&
					edge.getWeight() < minDistance) {

				minDistance = edge.getWeight();
			}
		}
		return minDistance;
	}

	/**
	 * Saca el path de los atributos target a source
	 * @return Lista de nodos con el path
	 * */
	public LinkedList<Node> getPath() {
		System.out.println("Distancias" + distance.toString());
		LinkedList<Node> path = new LinkedList<Node>();
		Node step = this.target;
		double minDistance  = Double.MAX_VALUE;
		for(Node i : predecessors.keySet()){
			if(i.getId().split("¿")[0].compareTo(this.target.getId()) == 0 &&
					predecessors.get(i) != null &&
					distance.get(i) < minDistance){
				step = i;
				minDistance = distance.get(i);
			}
		}
		path.add(step);
		while (step != this.source) {
			step = predecessors.get(step);
			path.add(step);
		}
		// Put it into the correct order
		Collections.reverse(path);
		return path;
	}
}
