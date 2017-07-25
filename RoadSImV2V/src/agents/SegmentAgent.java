package agents;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;

import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.json.JSONArray;
import org.json.JSONObject;

import behaviours.SegmentListenBehaviour;
import behaviours.SegmentRadarBehaviour;
import behaviours.SegmentSendToDrawBehaviour;
import environment.Intersection;
import environment.Segment;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jgrapht.Edge;
import vehicles.CarData;

/**
 * This agent will keep track of the cars that are inside between two
 * intersections and will update the data accordingly.
 *
 */
public class SegmentAgent extends Agent {

	private static final long serialVersionUID = 5681975046764849101L;

	//The segment this agent belongs to
	private Segment segment;
	private boolean drawGUI;
	//The log agent if it is requested on main
	private boolean useLog;
	private DFAgentDescription logAgent;

	//The cars that are currently on this segment
	private HashMap<String, CarData> cars;
	private DirectedWeightedMultigraph<Intersection, Edge> jgrapht;
	private HashMap<String, ArrayList<String>> interactingCars;
	//TODO: Gestionar lo que se guarda en el log
	private int serviceLevelPast;
	private long tini;

	
	protected void setup() {

		//Get the segment from parameter
		this.segment = (Segment) this.getArguments()[0];
		this.drawGUI = (boolean) this.getArguments()[1];
		this.jgrapht = (DirectedWeightedMultigraph<Intersection, Edge>) this.getArguments()[2];
		this.useLog = (boolean) this.getArguments()[3];
		this.setTini((long) this.getArguments()[4]);
		this.segment.setSegmentAgent(this);
		// X is a service level inexistent to obligate to log the first service level
		this.serviceLevelPast = 'X';

		this.cars = new HashMap<String, CarData>();

		//Register the service
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());

		ServiceDescription sd = new ServiceDescription();
		sd.setType("segmentAgent");

		sd.setName(this.getSegment().getId());

		dfd.addServices(sd);

		try {
			DFService.register(this,  dfd);
		} catch (FIPAException fe) { 
			fe.printStackTrace(); 
		}
		
		if (useLog) { //Find the log agent
			dfd = new DFAgentDescription();
			sd = new ServiceDescription();
			sd.setType("logAgent");
			dfd.addServices(sd);
	
			DFAgentDescription[] result = null;
	
			try {
				result = DFService.searchUntilFound(
						this, getDefaultDF(), dfd, null, 5000);
			} catch (FIPAException e) { e.printStackTrace(); }
	
			while (result == null || result[0] == null) {
				try {
					result = DFService.searchUntilFound(
							this, getDefaultDF(), dfd, null, 5000);
				} catch (FIPAException e) { e.printStackTrace(); }
			}
			
			this.logAgent = result[0];
		}
		
		//The id of the car and its id cars comunication
		interactingCars = new HashMap<String, ArrayList<String>>();
		
		//This behaviour will keep the cars updated	
		addBehaviour(new SegmentListenBehaviour(this));

		//This behaviour will send the data to the GUI
		if(this.drawGUI){
			addBehaviour(new SegmentSendToDrawBehaviour(this));
		}
		//This behaviour will answer car requests on neighbour cars 
		//   driving on twin segments
		addBehaviour(new SegmentRadarBehaviour(this));
	}

	/**
	 * Add a car to this segment
	 * 
	 * @param id ID of the car (getName() of the carAgent
	 * @param x X coordinate of the car
	 * @param y Y coordinate of the car
	 * @param specialColor If we have to paint it specially
	 * @param radio is the radio of its sensor
	 */
	/**
	 * Add a car to this segment
	 * 
	 * @param id ID of the car (getName() of the carAgent
	 * @param x X coordinate of the car
	 * @param y Y coordinate of the car
	 * @param specialColor If we have to paint it specially
	 * @param radio is the radio of its sensor
	 */
	public void addCar(JSONObject car) {

		this.cars.put(car.getString("id"), 
				new CarData(car.getString("id"), 
						    (float) car.getDouble("x"),
						    (float) car.getDouble("y"),
						    (float) car.getDouble("speed"),
						    (int) car.getInt("type"),
						    (float) car.getDouble("segmentDistanceCovered"),
						    (int) car.getInt("radio"),
						    (long) car.getLong("initialTick"),
						    car.getLong("tick")));
	}

	/**
	 * Remove a car from this segment
	 * 
	 * @param id ID of the car to remove
	 */
	public void removeCar(String id) {

		this.cars.remove(id);
		interactingCars.remove(id);
	}

	/**
	 * Check if the car is contained in this segment
	 * 
	 * @param id ID of the car to check
	 * @return True if found, false otherwise
	 */
	public boolean containsCar(String id) {

		return this.cars.containsKey(id);
	}

	/**
	 * Updates the information of a car
	 * 
	 * @param JSONObject of a CarData objetc
	 */
	public void updateCar(JSONObject car) {

		CarData aux = cars.get(car.getString("id"));
		aux.setX((float) car.getDouble("x"));
		aux.setY((float) car.getDouble("y"));
		aux.setCurrentSpeed((float) car.getDouble("speed"));
		aux.setSegmentDistanceCovered(
				(float) car.getDouble("segmentDistanceCovered"));
		aux.setRadio((int) car.getInt("radio"));
		aux.setInitialTick((long) car.getLong("initialTick"));
		aux.setCurrentTick(car.getLong("tick"));
	}

	/**
	 * Creates the string with the information about this segment to
	 * notify the InterfaceAgent
	 * 
	 * @return String with the information of this segment
	 */
	public String getDrawingInformation() {
		JSONObject resp = new JSONObject();
		JSONArray ret = new JSONArray();

		for(CarData car: cars.values()) {
			JSONObject ret2 = new JSONObject();
			ret2.put("id", car.getId());
			ret2.put("x", car.getX());
			ret2.put("y", car.getY());
			ret2.put("algorithmType", car.getTypeOfAlgorithm());
			ret.put(ret2);
		}
		
		resp.put("cars", ret);
		return resp.toString();
	}

	/**
	 * This method logs the information of the segment.
	 * 
	 * @return
	 */
	public void doLog(long currentTick) {
		//System.out.println("Falta implementar el log del segment");
		/*if (currentTick % 15 == 0) {

			if (currentTick % 15 == 0) {

				JSONObject data = new JSONObject();
				data.put("id", getLocalName());
				data.put("time", currentTick);
				data.put("currentSpeed", segment.getCurrentAllowedSpeed());
				data.put("maxSpeed", segment.getMaxSpeed());
				
				List<CarData> lista = 
						new ArrayList<CarData>(cars.values());
				java.util.Map<Integer, Long> counted = 
						lista.stream().
			            collect(Collectors.groupingBy(	            		
			            		(x->x.getTypeOfAlgorithm()), 
			            		Collectors.counting()));

				data.put("shortest", counted.get(0)==null?0:counted.get(0));
				data.put("fastest", counted.get(1)==null?0:counted.get(1));
				data.put("startSmart", counted.get(2)==null?0:counted.get(2));
				data.put("dynamicSmart", counted.get(3)==null?0:counted.get(3));
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.setOntology("segmentToLog");
				msg.addReceiver(logAgent.getName());
//				System.out.println("msg: " + data.toString());
				msg.setContent(data.toString());
				send(msg);
			}
		}*/
	}

	/**
	 * Number of cars in this segment
	 * 
	 * @return Number of cars in this segment
	 */
	public int carsSize() {

		return this.getCars().size();
	}

	//Getters and setters
	public Segment getSegment() {
		return segment;
	}

	public void setSegment(Segment segment) {
		this.segment = segment;
	}

	public HashMap<String, CarData> getCars() {
		return cars;
	}

	public DFAgentDescription getLogAgent() {
		return logAgent;
	}

	public void setLogAgent(DFAgentDescription logAgent) {
		this.logAgent = logAgent;
	}

	public int getServiceLevelPast() {
		return serviceLevelPast;
	}

	public void setServiceLevelPast(int serviceLevelPast) {
		this.serviceLevelPast = serviceLevelPast;
	}

	public boolean isUseLog() {
		return useLog;
	}

	public void setUseLog(boolean useLog) {
		this.useLog = useLog;
	}

	public DirectedWeightedMultigraph<Intersection, Edge> getJgrapht() {
		return jgrapht;
	}

	public void setJgrapht(DirectedWeightedMultigraph<Intersection, Edge> jgrapht) {
		this.jgrapht = jgrapht;
	}

	public long getTini() {
		return tini;
	}

	public void setTini(long tini) {
		this.tini = tini;
	}

	public boolean isNewCommunication(String idCar, String otherCar) {
		if(interactingCars.get(otherCar) == null){
			ArrayList<String> aux = new ArrayList<String>();
			aux.add(idCar);
			interactingCars.put(otherCar, aux);
			return true;
			
		} else if (!interactingCars.get(otherCar).contains(idCar)) {
			// Una vez ha habido una nueva interacción, esta deja de ser nueva por lo que se le añade a la lista de interacciones
			interactingCars.get(otherCar).add(idCar);
			return true;
		}
		return false;
	}
	
	public void addInteractionCar(String idSolicitante, String id) {
		interactingCars.get(idSolicitante).add(id);
	}
		
}
