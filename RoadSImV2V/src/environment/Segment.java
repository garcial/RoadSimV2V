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

	//The steps that form the segment
	private List<Step> steps;

	//Max speed
	private int maxSpeed;
	
	//Current allowed speed
	private int currentAllowedSpeed;

	//Kilometric points
	private int pkMin, pkMax;

	//Segment agent
	private SegmentAgent segmentAgent;

	//The container where the agents will be created
	@SuppressWarnings("unused")
	private transient jade.wrapper.AgentContainer mainContainer;
	
	//This dictionary contains the different service levels
	private HashMap<Character, Float> serviceLevels;
	
	//The current service level
	private Character currentServiceLevel;
	
	//Logging info
	private boolean segmentLogging;
	
	private String loggingDirectory;
	
	private List<String> twinSegments;
	
	public List<String> getTwinSegments() {
		return twinSegments;
	}

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
		this.pkMin = 0;
		this.pkMax = 0;
		this.mainContainer = null;
		this.currentAllowedSpeed = this.maxSpeed;
		this.serviceLevels = new HashMap<Character, Float>();
		this.currentServiceLevel = 'A';
		this.twinSegments = new LinkedList<String>();
	}

	/**
	 * Constructor. 
	 *
	 * @param  origin {@link Intersection} where this {@link Segment} starts.
	 * @param  destination {@link Intersection} where this {@link Segment} ends.
	 * @param  length The length of this {@link Segment} in Km.
	 */
	public Segment(String id, Intersection origin, Intersection destination, 
			       double length, int maxSpeed, int capacity, int density, 
			       int numberTracks, jade.wrapper.AgentContainer mainContainer, 
			       boolean segmentLogging, String loggingDirectory){

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
		this.serviceLevels = new HashMap<Character, Float>();
		this.currentServiceLevel = 'A';
		this.segmentLogging = segmentLogging;
		this.loggingDirectory = loggingDirectory;
		this.twinSegments = new LinkedList<String>();
		
		//Put the service levels
		this.serviceLevels.put('A', 1.00f);
		this.serviceLevels.put('B', 0.95f);
		this.serviceLevels.put('C', 0.80f);
		this.serviceLevels.put('D', 0.65f);
		this.serviceLevels.put('E', 0.50f);
		this.serviceLevels.put('F', 0.10f);

		//Create the agents
		try {

			AgentController agent = mainContainer.createNewAgent(
					this.id, "agents.SegmentAgent", new Object[]{this});

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

	public int getPkMin() {
		return pkMin;
	}

	public int getPkMax() {
		return pkMax;
	}

	public SegmentAgent getSegmentAgent() {
		return segmentAgent;
	}

	public void setSegmentAgent(SegmentAgent segmentAgent) {
		this.segmentAgent = segmentAgent;
		this.segmentAgent.setSegment(this);
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

	public Character getCurrentServiceLevel() {
		return currentServiceLevel;
	}

	public void setCurrentServiceLevel(Character currentServiceLevel) {
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
}
