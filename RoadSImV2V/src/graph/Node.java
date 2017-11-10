package graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/*
 * Esta clase representa a una interseccion de una carretera
 */
public class Node {
	
	// El id de la interseccion asociada univocamente a este nodo.
	private String id;
	// Los ids de los arcos de entrada a la interseccion.
	// Cada id es el id del segmento asociado a ese arco.
	private List<String> segmentIn;
	// Los ids de los arcos de salida de la interseccion.
	// Cada id es el id del segmento asociado a ese arco.
	private List<String> segmentOut;
	// La lista de caminos (por ids de arcos) permitidos desde cada 
	//    entrada a cada salida
	private Map<String, ArrayList<String>> allowedWays;
	
	public Node(String id){
		this.id = id;
		this.segmentIn = new ArrayList<String>();
		this.segmentOut = new ArrayList<String>();
		this.allowedWays = new HashMap<String, ArrayList<String>>();
	}
	
	public Node(String id, List<String> segmentIn, List<String> segmentOut) {
		this.id = id;
		this.segmentIn = segmentIn;
		this.segmentOut = segmentOut;
		this.allowedWays = new HashMap<String, ArrayList<String>>();
	}
	
	public void addInSegment(String s){
		this.segmentIn.add(s);
	}
	
	public void addOutSegment(String s){
		this.segmentOut.add(s);
	}

	
	/**
	 * Anyadir a un camino de entrada/salida valido a este nodo en el 
	 * atributo allowedWays.
	 * @param source id del arco de entrada a este nodo.
	 * @param target id del arco de salida de este nodo.
	 */
	public void addAllowedWay(String source, String target){
		if (!segmentIn.contains(source) || !segmentOut.contains(target)) {
			System.out.println("Error: source: " + source + " or target: " + 
		                       target + " wrong in Node: " + id);
			System.exit(0);
		}
		if(!this.allowedWays.containsKey(source)){
			ArrayList<String> aux = new ArrayList<String>();
			aux.add(target);
			this.allowedWays.put(source, aux);
		} else {
			this.allowedWays.get(source).add(target);
		}
	}

	/**
	 * Compute the list of the output edge's ids
	 * @param source source edge id
	 * @return A list with the allowed edge's ids
	 * 
	 */
	public List<String> getAllowedSegments(String source){
		List<String> edges = new ArrayList<String>();
		if(this.allowedWays.get(source) != null){
			edges = this.allowedWays.get(source); 
		}
		return edges;
	}

	/** Getters y Setters de los atributos */

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<String> getSegmentIn() {
		return segmentIn;
	}

	public void setSegmentIn(List<String> segmentIn) {
		this.segmentIn = segmentIn;
	}

	public List<String> getSegmentOut() {
		return segmentOut;
	}

	public void setSegmentOut(List<String> segmentOut) {
		this.segmentOut = segmentOut;
	}

	public Map<String, ArrayList<String>> getAllowedWays() {
		return allowedWays;
	}

	public void setAllowedWays(Map<String, ArrayList<String>> allowedWays) {
		this.allowedWays = allowedWays;
	}

	@Override
	public String toString() {
		return "(Node = " + id +  ")";
	}
	
	
}
