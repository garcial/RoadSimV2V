package graph;

import java.util.ArrayList;
import java.util.List;
import environment.EdgeData;

public class Edge {

	/**
	 * The edge was composed by the segment and its current density in a segment
	 * -- The segment that represent this Edge weight -- Time to go to the
	 * destinationedgeDataList -- List of data of others cars about this segment
	 */
	private String idSegment;
	private double weight;
	private int serviceLevel;
	// List of the past edge data to log the results
	private List<EdgeData> pastEdgeData;
	private long tini;
	private long tfin;

	public Edge(String idSegment, double weight,
				int serviceLevel, long tini, long tfin) {
		this.idSegment = idSegment;
		this.serviceLevel = serviceLevel;
		this.weight = weight;
		this.pastEdgeData = new ArrayList<EdgeData>();
		this.tini = tini;
		this.tfin = tfin;
	}

	public String getIdSegment() {
		return idSegment;
	}

	/**
	 * Actualiza el Edge con nuevos datos. Primero mete los datos que existian
	 * en la lista de Edge data para tener un log del pasado
	 * */
	public boolean updateEdge(String idSegment, int serviceLevel, double weight,
							  long initialDate, long finalDate) {
		if (idSegment.equals(this.idSegment)) {
			EdgeData ed = new EdgeData(
					getServiceLevel(), getWeight(), getTini(), getTfin());
			this.addEdgeData(ed);
			this.setServiceLevel(serviceLevel);
			this.setWeight(weight);
			this.setTini(tini);
			this.setTfin(tfin);
			return true;
		}
		return false;
	}

	/** Añade al log un nuevo EdgeData*/
	public void addEdgeData(EdgeData e) {
		this.pastEdgeData.add(e);
	}

	/** Coge la lista de datos del log de EdgeData*/
	public List<EdgeData> getPastEdgeData() {
		return pastEdgeData;
	}

	/**
	 * Los metodos siguientes son getters y setters de los atributos
	 * */

	public double getWeight() {
		return weight;
	}

	public void setWeight(double length) {
		this.weight = length;
	}

	public void setIdSegment(String idSegment) {
		this.idSegment = idSegment;
	}

	public int getServiceLevel() {
		return serviceLevel;
	}

	public void setServiceLevel(int serviceLevel) {
		this.serviceLevel = serviceLevel;
	}

	public long getTini() {
		return tini;
	}

	public void setTini(long tini) {
		this.tini = tini;
	}

	public long getTfin() {
		return tfin;
	}

	public void setTfin(long tfin) {
		this.tfin = tfin;
	}

	@Override
	public String toString() {
		return "Edge [idSegment=" + idSegment + ", weight=" + weight +
				", serviceLevel=" + serviceLevel + ", edgeDataList=" + 
				pastEdgeData.toString() + "]";
	}

}