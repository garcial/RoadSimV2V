package environment;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import agents.SegmentAgent;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

/**
 * Represents a section of a road in a single direction.
 * This section is only accessible from its origin and can only be 
 * left by its destination.
 */
public class Segment implements Serializable{

	private static final long serialVersionUID = -6853406084306746147L;

	//Unique id
	private String id;

	//Where the segment is accessed from
	private Intersection origin;

	//Where the segment is left
	private Intersection destination;

	//Length in kilometers of the segment
	private double length;

	//Capacity
	private int capacity;
	
	//Density
	private double density;

	//Number of tracks
	private int numberTracks;

	//The stepsTest that form the segment
	private List<Step> steps;

	//Max speed
	private int maxSpeed;
	
	//Current allowed speed
	private int currentAllowedSpeed;

	//Kilometric points
	private float pkIni;
	
	//Direction
	private String direction;

	//Variable to draw the GUI
	private boolean drawGUI;
	
	//List with the twins segmentsTest
	private List<String> twinSegments;
	
	//Road Code
	private String roadCode;

	//Segment agent
	private SegmentAgent segmentAgent;

	//The container where the agents will be created
	@SuppressWarnings("unused")
	private transient jade.wrapper.AgentContainer mainContainer;
	
	//This dictionary contains the different service levels
	private HashMap<Integer, Float> serviceLevels;
	
	//The current service level
	private int currentServiceLevel;
	
	//Logging info
	private boolean segmentLogging;
	
	private String loggingDirectory;
	
	private double weight;

	/**
	 * Default constructor. 
	 */
	public Segment(){

		this.id = "";
		this.origin = new Intersection();
		this.destination = new Intersection();
		this.length = 0.0;
		this.capacity = 0;
		this.numberTracks = 0;
		this.steps = new LinkedList<Step>();
		this.maxSpeed = 0;
		this.pkIni = 0;
		this.direction = "up";
		this.mainContainer = null;
		this.currentAllowedSpeed = this.maxSpeed;
		this.serviceLevels = new HashMap<Integer, Float>();
		this.currentServiceLevel = 0;
		this.segmentLogging = true;
		this.twinSegments = new LinkedList<String>();
		this.drawGUI = true;
		this.roadCode = "";
		this.weight = 0.0;
	}

	/**
	 * Constructor. 
	 *
	 * @param  origin {@link Intersection} where this {@link Segment} starts.
	 * @param  destination {@link Intersection} where this {@link Segment} ends.
	 * @param  length The length of this {@link Segment} in Km.
	 */
	public Segment(String id, Intersection origin, 
			Intersection destination, double length, int maxSpeed, int capacity,
			int density, int numberTracks, jade.wrapper.AgentContainer mainContainer, 
	        boolean segmentLogging, String loggingDirectory, boolean drawGUI,
	        String direction, double pkstart, LinkedList<String> segTwinsList, 
	        String roadCode, long tick, int serviceLevel) {

		this.id = id;
		this.origin = origin;
		this.destination = destination;
		this.length = length;
		this.maxSpeed = maxSpeed;
		this.capacity = capacity;
		this.density = density;
		this.numberTracks = numberTracks;
		this.steps = new LinkedList<Step>();
		this.mainContainer = mainContainer;
		this.currentAllowedSpeed = this.maxSpeed;
		this.serviceLevels = new HashMap<Integer, Float>();
		//Defaul currentService
		this.currentServiceLevel = serviceLevel;
		this.segmentLogging = segmentLogging;
		this.loggingDirectory = loggingDirectory;
		this.drawGUI = drawGUI;
		this.direction = direction;
		this.pkIni = (float) pkstart;
		this.twinSegments = segTwinsList;
		this.roadCode = roadCode;
		// The weight of the segment is, by defect, the time to traverse it
		//   at maximum speed.
		this.weight = length / maxSpeed;
		
		//Put the service levels
		this.serviceLevels.put(0, 1.00f);
		this.serviceLevels.put(1, 0.95f);
		this.serviceLevels.put(2, 0.80f);
		this.serviceLevels.put(3, 0.65f);
		this.serviceLevels.put(4, 0.50f);
		this.serviceLevels.put(5, 0.10f);

		//Create the agents
		try {

			//Agent Controller to segmentsTest with Interface
			AgentController agent = mainContainer.createNewAgent(
					this.id, "agents.SegmentAgent", new Object[]{this, this.drawGUI,
							 this.segmentLogging, tick});

			agent.start();
			
		} catch (StaleProxyException e) {

			System.out.println("Error starting " + this.id);
			e.printStackTrace();
		}
	}

	public void addStep(Step step) {
		this.steps.add(step);
	}

	//Setters and getters
	public String getId() {
		return id;
	}

	public Intersection getOrigin() {
		return origin;
	}

	public Intersection getDestination() {
		return destination;
	}

	public double getLength() {
		return length;
	}

	public int getCapacity() {
		return capacity;
	}

	public int getNumberTracks() {
		return numberTracks;
	}

	public List<Step> getSteps() {
		return steps;
	}

	public void setSteps(List<Step> steps) {
		this.steps = steps;
	}

	public int getMaxSpeed() {
		return maxSpeed;
	}

	public float getPkIni() {
		return pkIni;
	}

	public SegmentAgent getSegmentAgent() {
		return segmentAgent;
	}

	public void setSegmentAgent(SegmentAgent segmentAgent) {
		this.segmentAgent = segmentAgent;
		this.segmentAgent.setSegment(this);
	}

	public HashMap<Integer, Float> getServiceLevels() {
		return serviceLevels;
	}

	public void setServiceLevels(HashMap<Integer, Float> serviceLevels) {
		this.serviceLevels = serviceLevels;
	}

	public double getDensity() {
		return density;
	}

	public void setDensity(double density) {
		this.density = density;
	}

	public int getCurrentAllowedSpeed() {
		return currentAllowedSpeed;
	}

	public void setCurrentAllowedSpeed(int currentAllowedSpeed) {
		this.currentAllowedSpeed = currentAllowedSpeed;
	}

	public int getCurrentServiceLevel() {
		return currentServiceLevel;
	}

	public void setCurrentServiceLevel(int  currentServiceLevel) {	
		this.currentServiceLevel = currentServiceLevel;
		this.currentAllowedSpeed = (int) 
				(this.maxSpeed * this.serviceLevels.get(currentServiceLevel));
	}

	public boolean isSegmentLogging() {
		return segmentLogging;
	}

	public String getLoggingDirectory() {
		return loggingDirectory;
	}
	
	public List<String> getTwinSegments() {
		return twinSegments;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public String getRoadCode() {
		return roadCode;
	}

	public void setRoadCode(String roadCode) {
		this.roadCode = roadCode;
	}

	public double getWeight() {
		return weight;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	@Override
	public String toString() {
		return "Segment [id=" + id + "]";
	}
		
}
