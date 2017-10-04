package jgrapht;

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

	    private List<Node> nodes;
		private Set<Node> settledNodes;
	    private Set<Node> unSettledNodes;
	    // El segmento seleccionado es el segmento por el que vas
	    private Edge segmentSelected; 
	    private Map<Node, Node> predecessors;
	    // La key de las distancias es el id de la intersección - id del segmento
	    // El segmento que se utiliza en las distacias es que segmento por el cual llegas a esa intersección
	    //En el caso de que sea el primer nodo que pongo ¿Solo el nombre de la intersección 0 o los de entrada?
	    private Map<Node, Double> distance;
	    
	    private Node source;
	    private Node target;

	    public DijkstraGirosPermitidos(MultiGraphRoadSim graph) {
	        // create a copy of the array so that we can operate on this array
	    	// Ejemplo de Segmento - Edge {"2","3","E3","2"}
	    	//{Intersección origen, destino, id, peso}
	    	this.nodes = graph.getNodes();
	    	this.unSettledNodes = new HashSet<Node>();
	    	this.distance = new HashMap<Node, Double>();
	        this.predecessors = new HashMap<Node, Node>();
	        for(Edge i : graph.getEdges()){
	        	// Los nodos son la intersección destino y el camino por el que llegan ahí
	        	Node inter = new Node(i.getDestination().getId() + "-" + i.getIdSegment());
	        	this.unSettledNodes.add(inter);
	        	this.distance.put(inter, Double.MAX_VALUE);
	        	this.predecessors.put(inter, null);
	        }
	    	
	    }

	    public void execute(Node source, Node target) {
	    	this.source = source;
	    	this.target = target;
	    	this.segmentSelected = null;
	        this.settledNodes = new HashSet<Node>();
	        System.out.println("------NODOS NO VISITADOS INICIO-------");
	        System.out.println(this.unSettledNodes);
	        System.out.println("--------------------------------------");
	        
	        unSettledNodes.add(source);
	        distance.put(source, 0.0);
	        while (unSettledNodes.size() > 0) {
	        	/* Busca el nodo de menor peso */
	        	System.out.println("{ { .UNSETTLENODES:" + this.unSettledNodes);
	            Node node = getMinimum(unSettledNodes);
	            /* Recalcula los pesos de sus vecinos */
	            findMinimalDistances(node);
	            System.out.println("{   {  .  .  .NODE: " + node);
	            System.out.println("{{··· PREDECESORES: " + this.predecessors);
	            System.out.println("{  {  ·  ·  ·PESOS: " + this.distance);
	            if(node.getId().compareTo(target.getId().split("-")[0]) == 0)
	            	break;
	        }
	    }
	    
	    /**
	     * Este método busca el nodo con menor peso de los no recorridos
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
	     * */
	    private double getShortestDistance(Node destination) {
	    	return distance.get(destination);
	    }

	    private void findMinimalDistances(Node node) {
	    	System.out.println("FindMinimalDistance de " + node.toString());
	        List<Node> adjacentNodes = getNeighbors(node);
	        System.out.println("Vecinos de " + node.getId() + "  --> " + adjacentNodes);
	        for (Node target : adjacentNodes) {
	        	System.out.println("//FIND MINIMAL DISTANCES - TARGET - " + target);
	        	double shortestNode = getShortestDistance(node);
	        	System.out.println("//DE ShortestDistanceDe " + node.getId() + " : " + shortestNode);
	        	double shortestTarget = getShortestDistance(target);
	        	System.out.println("//A ShortestDistanceDe " + target.getId() + " : " + shortestTarget);
	        	double distandeNodeTarget = getDistance(node,target);
	        	System.out.println("//ShortestDistanceDe " + node.getId() + " A " + target.getId() + " : " + distandeNodeTarget);
	            System.out.println("ShortestTarget > shortestNode + distanceNodeTarget");
	            System.out.println(shortestTarget + " > " + shortestNode + " + " + distandeNodeTarget);
	        	if (shortestTarget > shortestNode + distandeNodeTarget) {
	        		distance.put(target, shortestNode + distandeNodeTarget);
		            predecessors.put(target, node);
	            }
	        }
	        unSettledNodes.remove(node);
	    }
	    
	    private List<Node> getNeighbors(Node node) {
	        System.out.println("GetNeighbors - " + node);
	    	List<Node> neighbors = new ArrayList<Node>();
	        List<Edge> candidates = new ArrayList<Edge>();
	        if(node.getId().compareTo(this.source.getId()) == 0){
	        	candidates = node.getSegmentOut();
	        }else{
	        	//System.out.println(" ++No es el primero");
	        	Node interseccion = null;
	        	for (Node nodeTest: this.nodes){
	        		if(nodeTest.getId().compareTo(node.getId().split("-")[0]) == 0){
	        			interseccion = nodeTest;
	        		}
	        	}
	        	System.out.println(" ++Interseccion: " + interseccion);
	        	System.out.println(" ++AllowedWays: " + interseccion.getAllowedWays());
	        	System.out.println(" ++SEGMENT: " + interseccion.getSegmentById(node.getId().split("-")[1]));
	        	List<Edge> listSegments = interseccion.getAllowedSegments(interseccion.getSegmentById(node.getId().split("-")[1]));
	        	System.out.println(" ++ListSegments: " + listSegments);
	        	for(Edge s: listSegments){
	        			candidates.add(s);
	        	}
	        }
	        
	        for (Edge edge : candidates) {
	                Iterator<Node> iter = this.unSettledNodes.iterator();
	                while(iter.hasNext()){
	                	Node cand = (Node) iter.next();
	                	if(cand.getId().compareTo(edge.getDestination().getId() + "-" + edge.getIdSegment()) == 0){
	                		neighbors.add(cand);
	                	}
	                }
	        }
	       
	        return neighbors;
	    }

	    private double getDistance(Node node, Node target) {
	    	double minDistance = distance.get(target);
	    	System.out.println("-Distancia Minima: " + minDistance);
	    	System.out.println("-Source: " + node);
	    	System.out.println("-Target: " + target);
	    	
	    	ArrayList<Edge> neighborsSegments = new ArrayList<Edge>();
	    	String[] nodeParts = node.getId().split("-"); // En el caso del primer nodo solo tiene la id de la intersección
	    	String[] targetParts = target.getId().split("-");
	    	
	    	System.out.println("-TargetParts: " + targetParts[0] + " " + targetParts[1] );
	    	// The parts of the target are [intersectionDestination, segmentFromOriginToDestination]
	    	if(nodeParts[0].compareTo(this.source.getId()) == 0){
	    		neighborsSegments = (ArrayList<Edge>) node.getSegmentOut();
	    	} else {
	    		System.out.println("-SourceParts: " + nodeParts[0] + " " + nodeParts[1]);
	    		System.out.println("No es el primero");
	    		Node nodePrimary = null;
	    		for(Node nodeTest : this.nodes){
	    			if(nodeTest.getId().compareTo(nodeParts[0]) == 0){
	    				nodePrimary = nodeTest;
	    				break;
	    			}
	    		}
	    		System.out.println("NodePrimary: " + nodePrimary);
	    		System.out.println("NodePrimaryAllowe: " + nodePrimary.getAllowedWays());
	    	
	    		neighborsSegments.addAll(nodePrimary.getAllowedSegments(nodePrimary.getSegmentById(nodeParts[1])));	
	    	}
	    	System.out.println(" .getDistance::Vecinos -- " + neighborsSegments);
	        for (Edge edge : neighborsSegments) {
	            /*System.out.println(" .Source: " + node + " EdgeSource: " + edge.getSource());
	            System.out.println(" .Target: " + target + " EdgeTarget: " + edge.getDestination());
	            System.out.println(" .Peso a minimizar: " + minDistance + " EdgeDistance: " + edge.getWeight());*/
	        	if (edge.getIdSegment().equals(targetParts[1]) && edge.getDestination().getId().equals(targetParts[0]) && edge.getWeight() < minDistance) {
	            	System.out.println(" .Segment Vecino: " + edge.getIdSegment() + " de " + node.getId() + " a " + target.getId());
	            	minDistance = edge.getWeight();
	                this.segmentSelected = edge;
	            } else {
	            	System.out.println(" .No cumple los requisitos el seg " + edge.getIdSegment());
	            }
	        }
	        return minDistance;
	    }
	    
	    /*
	     * This method returns the path from the source to the selected target and
	     * NULL if no path exists
	     */
	    public LinkedList<Node> getPath(Node target) {
	        System.out.println("GetPath");
	        System.out.println(target);
	        System.out.println(predecessors);
	        System.out.println("Nodes: " + this.nodes);
	        LinkedList<Node> path = new LinkedList<Node>();
	        Node step = target;
	        for(Node i : predecessors.keySet()){
	        	double minDistance  = Double.MAX_VALUE;
	        	
	        	if(i.getId().split("-")[0].compareTo(target.getId()) == 0 && predecessors.get(i) != null && distance.get(i) < minDistance){
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
	    
	    
	    
	    public LinkedList<Node> getPath() {
	        System.out.println("GetPath");
	        System.out.println(this.target);
	        System.out.println(predecessors);
	        System.out.println(distance);
	        LinkedList<Node> path = new LinkedList<Node>();
	        Node step = this.target;
	        double minDistance  = Double.MAX_VALUE;
	        for(Node i : predecessors.keySet()){
	        	if(i.getId().split("-")[0].compareTo(this.target.getId()) == 0 && predecessors.get(i) != null && distance.get(i) < minDistance){
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

		public Map<Node, Double> getDistance() {
			return distance;
		}

		public void setDistance(Map<Node, Double> distance) {
			this.distance = distance;
		}
	    
	   

}
