package agents;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import searchAlgorithms.Method;
import trafficData.TrafficData;
import trafficData.TrafficDataInStore;
import trafficData.TrafficDataOutStore;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONObject;

import behaviours.CarBehaviour;
import behaviours.CarReceivingDataBehaviour;
import behaviours.CarsAheadBehaviour;
import environment.Intersection;
import environment.TrafficMap;
import environment.Path;
import environment.Segment;
import environment.Step;
import features.CarData;
import features.SimulationData;
import features.TravelData;
import graph.*;

/**
 * This code represents a mobile car, it will have an origin an a 
 * destination and will get there using either the shortest, 
 * fastest or smartest path. This last by sharing traffic data 
 * with opposite way crossing carAgents.
 *
 */
public class CarAgent extends Agent {


	private static final long serialVersionUID = 1L;

	private CarData carData;
	private TravelData travelData;
	private SimulationData simulationData;
	private DFAgentDescription interfaceAgent;
	private DFAgentDescription logAgent;
	private TrafficMap map;
	private Path path;
	private boolean smart = false;
	private MultiGraphRoadSim graph;

	// This object stores current traffic sensored data
	// every time a car goes into a new segment, this object is
	// reseting.
	private TrafficData sensorTrafficData;

	// future: is for storing data received from other cars and
	//    used for computing my route to destination
	private TrafficDataInStore futureTraffic;

	// past: is for informing data to send to other cars about
	//    what is the traffic state in my performed route
    private TrafficDataOutStore pastTraffic;

	protected void setup() {

		//Register the agent
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("carAgent");
		sd.setName(getLocalName());

		dfd.addServices(sd);
		try {
			DFService.register(this,  dfd);
		} catch (FIPAException fe) {
			
			//Sometimes an agent cannot find the DF in time
			//I still don't know when this happens so I will
			//simply kill it for now.
			this.takeDown();
		}
		
		travelData = new TravelData();
		simulationData = new SimulationData();
		
		//Get the map from an argument
		this.map = (TrafficMap) this.getArguments()[0];
		
		// Get the graph from the map. 
		// Each car agent should have its own private graph
		// Make a deep clone of the graph contained into the map object

		try {
			graph = (MultiGraphRoadSim) deepClone(map.getGraph());
		} catch (Exception e1) {
			System.out.println("Something went wrong making a graph deep copy");
			e1.printStackTrace();
			this.takeDown();
		}
		
		//Get the starting and final points of my trip
		travelData.setInitialIntersection((String) this.getArguments()[1]);
		travelData.setFinalIntersection((String) this.getArguments()[2]);
		
		carData = new CarData();
		
		//Get the speeds
		carData.setMaxSpeed((int) this.getArguments()[3]);
		
		String routeType = (String) this.getArguments()[4];
		//Is necessary draw the gui
		simulationData.setUseGUI((boolean) this.getArguments()[5]);
		
		//Get the initial time whole travel tick from eventManager
		simulationData.setStartTick((long) this.getArguments()[6]);
		
		//Get the initial tick for the current segment
		simulationData.setInitialTick(simulationData.getStartTick());
		
		//Get the ratio of sensoring for this agentCar
		carData.setRadio((int) this.getArguments()[7]);
		
		//It is requested to do Logs?
		simulationData.setUseLog((boolean) this.getArguments()[8]);

		//Assign the type of algorithm
		
		if (routeType.equals("fastest")){
			carData.setTypeOfAlgorithm(Method.FASTEST.value);
		} else if (routeType.equals("shortest")){
			carData.setTypeOfAlgorithm(Method.SHORTEST.value);
		} else if (routeType.equals("dynamicSmart")) {
			carData.setTypeOfAlgorithm(Method.DYNAMICSMART.value);
			this.smart = true;
		} else {
			carData.setTypeOfAlgorithm(Method.STARTSMART.value);
			this.smart = true;
		}

	    this.path = getPathOnMethod(travelData.getInitialIntersection(), 
				                    travelData.getFinalIntersection());

		Step currentStep = path.getGraphicalPath().get(0);
		travelData.setCurrentSegment(currentStep.getSegment());
		travelData.setSegmentDistanceCovered(0);

		//Store data received from other cars in a Map
		futureTraffic = new TrafficDataInStore();

		//Store data to send to other cars in my route
		pastTraffic = new TrafficDataOutStore();
		
		// Store current trafficData sensored by myself
		sensorTrafficData = new TrafficData();
		carData.setId(getName().toString());
		carData.setX(currentStep.getOriginX());
		carData.setY(currentStep.getOriginY());
		carData.setCurrentSpeed( 
			Math.min(carData.getMaxSpeed()/4, // CurrentSpeed at the beginning
					 travelData.getCurrentSegment().getCurrentAllowedSpeed()));
		
		//Register
	    //Add the tini of the first segment
	    sensorTrafficData.setTini(simulationData.getInitialTick());
	    
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setOntology("carToSegmentOntology");
		msg.setConversationId("register");
		msg.addReceiver(currentStep.getSegment().getSegmentAgent().getAID());
		
		msg.setContent(carData.toJSON().toString());
		
		send(msg);
		// Receive the current traffic density from the current segment
		msg = blockingReceive(MessageTemplate.
				             MatchOntology("trafficCarsAheadOntology"));
		JSONObject densityData = new JSONObject(msg.getContent());
		travelData.setCarsAhead(densityData.getInt("cars"));
		
		if (simulationData.isUseLog()) {
			//Find the log agent
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
		
		if(simulationData.isUseGUI()){
			//Find the interface agent
			dfd = new DFAgentDescription();
			sd = new ServiceDescription();
			sd.setType("interfaceAgent");
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
			
			this.interfaceAgent = result[0];
		
			//We notify the interface about the new car
			msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(interfaceAgent.getName());
			msg.setContent(this.carData.toJSON().toString());
			msg.setOntology("newCarOntology");
			send(msg);
		}

		
		//Runs the agent
		addBehaviour(new CarBehaviour(this, 50, simulationData.isUseGUI()));
		addBehaviour(new CarReceivingDataBehaviour(this));
		addBehaviour(new CarsAheadBehaviour(this));

	}
	
	/**
	 * Recalculate the route, this will be called from the behaviour 
	 *     if we are smart.
	 * 
	 * @param origin ID of the intersection where the car is
	 */
	public void recalculate(String origin) {
//TODO: Modify weights on edges by futureTraffic estimations received from 
//      other carAgents		
		this.path = getPathOnMethod(origin,travelData.getFinalIntersection());
		//System.out.println(this.getFutureTraffic().getData().toString());
		//System.out.println(this.path.getSegmentPath().toString());
	}
	
	/**
	 * Make a deep copy of the object received as a parameter
	 *   by its serialization and its deserialization into a new 
	 *   Object
	 * @param obj to be deep copied
	 * @return the deep copy of obj
	 * @throws IOException when some element of the object is not
	 *         serializable
	 * @throws ClassNotFoundException when something went wrong reading 
	 *         the serialized object back again.
	 */
	public static Object deepClone(Object obj) 
			throws IOException, ClassNotFoundException {

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(obj);
			ByteArrayInputStream bais = 
					             new ByteArrayInputStream(baos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bais);
			return ois.readObject();
	}

	//Setters and getters

	public CarData getCarData() {
		return carData;
	}

	public void setCarData(CarData carData) {
		this.carData = carData;
	}
	
	public SimulationData getSimulationData() {
		return simulationData;
	}

	public void setSimulationData(SimulationData simulationData) {
		this.simulationData = simulationData;
	}
	
	public TravelData getTravelData() {
		return travelData;
	}

	public void setTravelData(TravelData travelData) {
		this.travelData = travelData;
	}

	public DFAgentDescription getInterfaceAgent() {
		return interfaceAgent;
	}

	public TrafficMap getMap() {
		return map;
	}

	public Path getPath() {
		return path;
	}
	
	public MultiGraphRoadSim getGraph() {
		return graph;
	}

	public void setjgrapht(MultiGraphRoadSim graph){
		this.graph = graph;
	}
	
	public boolean isSmart() {
		
		return this.smart;
	}

	public TrafficData getSensorTrafficData() {
		return sensorTrafficData;
	}

	public void setSensorTrafficData(TrafficData sensorTrafficData) {
		this.sensorTrafficData = sensorTrafficData;
	}

	public TrafficDataOutStore getPastTraffic() {
		return pastTraffic;
	}

	public void setPastTraffic(TrafficDataOutStore pastTraffic) {
		this.pastTraffic = pastTraffic;
	}

	public TrafficDataInStore getFutureTraffic() {
		return futureTraffic;
	}

	public void setFutureTraffic(TrafficDataInStore futureTraffic) {
		this.futureTraffic = futureTraffic;
	}

	public DFAgentDescription getLogAgent() {
		return logAgent;
	}

	public void setLogAgent(DFAgentDescription logAgent) {
		this.logAgent = logAgent;
	}

	
	public Path getPathOnMethod(String startInt, String endInt){
        LinkedList<Node> pathGrapht = null;
        if (carData.getTypeOfAlgorithm() == Method.SHORTEST.value) {
			putWeightsAsDistancesOnGraph(graph);
		} else if (carData.getTypeOfAlgorithm() == Method.FASTEST.value) {
			putWeightsAsSegmentsMaxSpeedsOnGraph(graph);
		}
		
		DijkstraGirosPermitidos dijkstra = new DijkstraGirosPermitidos(graph); 
		dijkstra.execute(graph.getNodeById(startInt), 
				         graph.getNodeById(endInt));
		pathGrapht = dijkstra.getPath();
		
		List<Step> steps = new ArrayList<Step>();
		List<Segment> segments = new ArrayList<Segment>();
		List<Intersection> intersections = new ArrayList<Intersection>();
		
		for(Node n: pathGrapht){
			String[] interEdge = n.getId().split("¿");
			intersections.add(map.getIntersectionByID(interEdge[0]));
			if(interEdge.length > 1){// Is not source // TODO: Or destiny????
				steps.addAll(map.getSegmentByID(interEdge[1]).getSteps());
				segments.add(map.getSegmentByID(interEdge[1]));
			}
		}
		
		return new Path(intersections,steps, segments);
	}

	//Used with the shortest method. The road speed is not important
	private void putWeightsAsDistancesOnGraph( MultiGraphRoadSim graph) {
		for(Edge e: graph.getEdges()) {
			e.setWeight(e.getWeight());
		}
	}
	
	//Used with the fastest method. The fast method depends of the distance 
	//  and the speed
	private void putWeightsAsSegmentsMaxSpeedsOnGraph(MultiGraphRoadSim graph) {
		for(Edge e: graph.getEdges()) {
			e.setWeight(e.getWeight() /
			            map.getSegmentByID(e.getIdSegment()).getMaxSpeed());
		}
	}

}
