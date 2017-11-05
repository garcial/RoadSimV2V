package features;

import environment.Segment;

public class TravelData {

	String initialIntersection;
	String finalIntersection;
	Segment currentSegment;
	float segmentDistanceCovered;
	float tripDistanceCovered;
	double currentTrafficDensity;
	float currentPK;
	int direction;
	int carsAhead;
	
	public int getCarsAhead() {
		return carsAhead;
	}
	public void setCarsAhead(int carsAhead) {
		this.carsAhead = carsAhead;
	}
	public void decreaseCarsAhead() {
		carsAhead--;
	}
	public String getInitialIntersection() {
		return initialIntersection;
	}
	public void setInitialIntersection(String initialIntersection) {
		this.initialIntersection = initialIntersection;
	}
	public String getFinalIntersection() {
		return finalIntersection;
	}
	public void setFinalIntersection(String finalIntersection) {
		this.finalIntersection = finalIntersection;
	}
	public Segment getCurrentSegment() {
		return currentSegment;
	}
	public void setCurrentSegment(Segment currentSegment) {
		this.currentSegment = currentSegment;
	}
	public float getSegmentDistanceCovered() {
		return segmentDistanceCovered;
	}
	public void setSegmentDistanceCovered(float segmentDistanceCovered) {
		this.segmentDistanceCovered = segmentDistanceCovered;
	}
	public void increaseSegmentDistanceCovered(float amount) {
		this.segmentDistanceCovered += amount;
	}
	public float getTripDistanceCovered() {
		return tripDistanceCovered;
	}
	public void setTripDistanceCovered(float tripDistanceCovered) {
		this.tripDistanceCovered = tripDistanceCovered;
	}
	public void increaseTripDistanceCovered(float deltaPk) {
		this.tripDistanceCovered += deltaPk;	
	}
	public double getCurrentTrafficDensity() {
		return currentTrafficDensity;
	}
	public void setCurrentTrafficDensity(double currentTrafficDensity) {
		this.currentTrafficDensity = currentTrafficDensity;
	}
	public float getCurrentPK() {
		return currentPK;
	}
	public void setCurrentPK(float currentPK) {
		this.currentPK = currentPK;
	}
	public int getDirection() {
		return direction;
	}
	public void setDirection(int direction) {
		this.direction = direction;
	}
	
	
}
