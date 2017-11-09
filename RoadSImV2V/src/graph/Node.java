package graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Node {
	// Nodo del grafo. Este nodo es la interseccion y
	// la lista de segmentos a los que puede ir
	private String id;
	private List<Edge> segmentIn;
	private List<Edge> segmentOut;
	private Map<String, ArrayList<String>> allowedWays;
	
	public Node(String id){
		this.id = id;
		this.segmentIn = new ArrayList<Edge>();
		this.segmentOut = new ArrayList<Edge>();
		this.allowedWays = new HashMap<String, ArrayList<String>>();
	}
	
	public Node(String id, List<Edge> segmentIn, List<Edge> segmentOut) {
		this.id = id;
		this.segmentIn = segmentIn;
		this.segmentOut = segmentOut;
		this.allowedWays = new HashMap<String, ArrayList<String>>();
	}
	
	public void addSegmentIn(Edge s){
		this.segmentIn.add(s);
	}
	
	public void addSegmentOut(Edge s){
		this.segmentOut.add(s);
	}

	/** Añade un camino permitido . En el caso de que no tenga
	 * caminos permitidos crea la lista y añade el camino. En
	 * el caso de que el nodo si que tenga caminos permitidos
	 * pues añade a los actuales caminos uno nuevo.
	 * @param source Origen del camino
	 * @param target Destino del camino*/
	public void addAllowedWay(String source, String target){
		if(!this.allowedWays.containsKey(source)){
			ArrayList<String> aux = new ArrayList<String>();
			aux.add(target);
			this.allowedWays.put(source, aux);
		} else {
			this.allowedWays.get(source).add(target);
		}
	}

	/**
	 * @param source El segmento del que partes para
	 *               conseguir los permitidos
	 * @return la lista de Segmentos permitidos
	 * */
	public List<Edge> getAllowedSegments(Edge source){
		ArrayList<Edge> res = new ArrayList<Edge>();
		List<String> segments = new ArrayList<String>();
		if(this.allowedWays.get(source.getIdSegment()) != null){
			segments = this.allowedWays.get(source.getIdSegment()); 
		}
		for(String s: segments){
			res.add(this.getSegmentById(s));
		}

		return res;
	}

	/** Getters y Setters de los atributos */

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public List<Edge> getSegmentIn() {
		return segmentIn;
	}

	public void setSegmentIn(List<Edge> segmentIn) {
		this.segmentIn = segmentIn;
	}

	public List<Edge> getSegmentOut() {
		return segmentOut;
	}

	public void setSegmentOut(List<Edge> segmentOut) {
		this.segmentOut = segmentOut;
	}

	public Map<String, ArrayList<String>> getAllowedWays() {
		return allowedWays;
	}

	public void setAllowedWays(Map<String, ArrayList<String>> allowedWays) {
		this.allowedWays = allowedWays;
	}

	/**
	 * @param id String del id del Segment
	 * @return Devuelve un Edge a partir del id */
	public Edge getSegmentById(String id){
		for(Edge s: this.segmentOut){
			if(s.getIdSegment().compareTo(id)==0){
				return s;
			}
		}
		for(Edge s: this.segmentIn){
			if(s.getIdSegment().compareTo(id)==0){
				return s;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return "(Inte=" + id +  ")";
	}
	
	
}
