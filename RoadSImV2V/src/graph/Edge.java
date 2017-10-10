package graph;

import java.util.ArrayList;
import java.util.List;
import org.jgrapht.graph.DefaultWeightedEdge;
import environment.EdgeData;

public class Edge extends DefaultWeightedEdge {

	/**
	 * The edge was composed by the segment and its current density in a segment
	 * -- The segment that represent this Edge weight -- Time to go to the
	 * destinationedgeDataList -- List of data of others cars about this segment
	 */
	private String idSegment;
	private double weight;
	private int serviceLevel;
	private int maxSpeed;
	// List of the past edge data to log the results
	private List<EdgeData> pastEdgeData;
	private static final long serialVersionUID = 17455L;
	private long tini;
	private long tfin;
	private Node initialNode;
	private Node finalNode;

	public Edge(Node initialNode, Node finalNode, String idSegment, double weight, int serviceLevel, int maxSpeed, long tini, long tfin) {
		this.initialNode = initialNode;
		this.finalNode = finalNode;
		this.idSegment = idSegment;
		this.serviceLevel = serviceLevel;
		this.weight = weight;
		this.maxSpeed = maxSpeed;
		this.pastEdgeData = new ArrayList<EdgeData>();
		this.tini = tini;
		this.tfin = tfin;
	}

	public String getIdSegment() {
		return idSegment;
	}

	public boolean updateEdge(String idSegment, int serviceLevel, double weight, int maxSpeed, long initialDate, long finalDate) {
		if (idSegment.equals(this.idSegment)) {
			EdgeData ed = new EdgeData(getServiceLevel(), getWeight(), getTini(), getTfin());
			this.addEdgeData(ed);
			this.setServiceLevel(serviceLevel);
			this.setMaxSpeed(maxSpeed);
			this.setWeight(weight);
			this.setTini(tini);
			this.setTfin(tfin);
			return true;
		}
		return false;
	}

	public void addEdgeData(EdgeData e) {
		this.pastEdgeData.add(e);
	}

	public List<EdgeData> getPastEdgeData() {
		return pastEdgeData;
	}

	public void setPastEdgeData(List<EdgeData> pastEdgeData) {
		this.pastEdgeData = pastEdgeData;
	}

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

	public int getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(int maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	public Node getSource() {
		return initialNode;
	}

	public Node getDestination() {
		return finalNode;
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
		return "Edge [idSegment=" + idSegment + ", weight=" + weight + ", serviceLevel=" + serviceLevel
				+ ", edgeDataList=" + pastEdgeData.toString() + ", initialNode=" + initialNode + ", finalNode=" + finalNode + "]";
	}

}