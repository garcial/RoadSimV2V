package jgrapht;

import org.jgrapht.graph.DefaultWeightedEdge;
import environment.Segment;

public class Edge extends DefaultWeightedEdge {

	/**
	 * The edge was composed by the segment and its density in a 
	 * moment segment -- The segment that represent this Edge
	 * weight -- Time to go to the destination
	 * edgeDataList -- List of data of others cars about this 
	 * segment
	 */
	private Segment segment;
	private float weight;
	private long tini;
	private long tfin;
	private int serviceLevel;
	private static final long serialVersionUID = 17455L;

	public Edge() {
		// TODO Auto-generated constructor stub
	}
	
	public Edge(Segment segment) {
		this.segment = segment;
		this.weight = (float) 0.0;
	}

	public Edge(Segment segment, char serviceLevel, 
			    long initialDate, long finalDate) {
		super();
		this.segment = segment;
		this.weight = 0.0f;
		this.serviceLevel = serviceLevel;
		this.tini = initialDate;
		this.tfin = finalDate;
		
	}
	
	@Override
	/**
	 * The objective is return the time estimated to do the travel
	 * */
	public double getWeight(){
		return weight;
	}

	public Segment getSegment() {
		return segment;
	}

	public void setSegment(Segment segment) {
		this.segment = segment;
	}
	
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public void setWeight(float weight) {
		this.weight = weight;
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

	public int getServiceLevel() {
		return serviceLevel;
	}

	public void setServiceLevel(int serviceLevel) {
		this.serviceLevel = serviceLevel;
	}

	@Override
	public String toString() {
		return "Edge [segment=" + segment.getId() + ", weight=" + weight + ", tini=" + tini + ", tfin=" + tfin
				+ ", serviceLevel=" + serviceLevel + "]";
	}

	

	

}
