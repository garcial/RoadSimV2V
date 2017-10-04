package agents;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import searchAlgorithms.Algorithm;
import searchAlgorithms.AlgorithmFactory;
import searchAlgorithms.Method;
import trafficData.TrafficData;
import trafficData.TrafficDataInStore;
import trafficData.TrafficDataOutStore;
import vehicles.CarData;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.json.JSONObject;

import behaviours.CarBehaviour;
import behaviours.CarReceivingDataBehaviour;
import environment.Intersection;
import environment.Map;
import environment.Path;
import environment.Segment;
import environment.Step;
import jgrapht.Edge;

/**
 * This code represents a mobile car, it will have an origin an a 
 * destination and will get there using either the shortest, 
 * fastest or smartest path.
 *
 */
public class CarAgent extends Agent {


	private static final long serialVersionUID = 1L;

	private CarData carData;
	private float currentPk;
	private int direction;
	private int ratio = 10; // El radio es un valor fijo que dependerá del hardware
	private int maxSpeed;
	private double currentTrafficDensity;
	private long tini; // For measuring temporal intervals of traffic
	private DFAgentDescription interfaceAgent;
	private DFAgentDescription logAgent;
	private boolean drawGUI;
	private boolean useLog;
	private Map map;
	private Algorithm alg;
	private Path path;
	private Segment currentSegment;
	private String initialIntersection, finalIntersection;
	private boolean smart = false;
	private int algorithmType;
	private DirectedWeightedMultigraph<Intersection, Edge> jgrapht;
	private long currentTick;
	
	private List<LogData> logData;
	private long logInitialTick;
	private long logEndTick;
	private String logAlgorithm;
	//TODO: Update the number of msg send a received
	private int numMsgRecibido = 0;
	private int numMsgEnviados = 0;

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
		
		//Get the map from an argument
		this.map = (Map) this.getArguments()[0];
		
		//Get the jgraph from the map
		this.jgrapht = (DirectedWeightedMultigraph<Intersection, Edge>) this.map.getJgrapht().clone();
		
		//Get the starting and final points of my trip
		this.initialIntersection = (String) this.getArguments()[1];
		this.finalIntersection = (String) this.getArguments()[2];
		
		//Get the speeds
		this.maxSpeed = (int) this.getArguments()[3];
		
		String routeType = (String) this.getArguments()[4];
		//Is necessary draw the gui
		this.drawGUI = (boolean) this.getArguments()[5];
		
		//Get the initial time tick from eventManager
		tini = (long) this.getArguments()[6];

		//Get the ratio of sensoring for this agentCar
		ratio = (int) this.getArguments()[7];
		
		//It is requested to do Logs?
		useLog = (boolean) this.getArguments()[8];


		this.path = getPathOnMethod(initialIntersection, 
			       finalIntersection);
		
		Step currentStep = path.getGraphicalPath().get(0);
	    setCurrentSegment(currentStep.getSegment());

		/* Generate the log parameters*/
		//Asign the type of algorithm
		this.logAlgorithm = routeType;
		this.logInitialTick = tini;

		//Store data received from other cars in a Map
		futureTraffic = new TrafficDataInStore();

		//Store data to send to other cars in my route
		pastTraffic = new TrafficDataOutStore();

		this.logData = new ArrayList<LogData>();

		// Store current trafficData sensored by myself
		sensorTrafficData = new TrafficData();
		// Tini for measuring traffic data intervals in twin segments 
		//tini = elapsedtime;
		
		AlgorithmFactory factory = new AlgorithmFactory();
		this.alg = null;
		
		if (routeType.equals("fastest")){
			this.alg = factory.getAlgorithm(Method.FASTEST);
			this.algorithmType = Method.FASTEST.value;
		} else if (routeType.equals("shortest")){
			this.alg = factory.getAlgorithm(Method.SHORTEST);
			this.algorithmType = Method.SHORTEST.value;
		} else if (routeType.equals("dynamicSmart")) {
			this.alg = factory.getAlgorithm(Method.DYNAMICSMART);
			this.algorithmType = Method.DYNAMICSMART.value;
		} else {
			this.alg = factory.getAlgorithm(Method.STARTSMART);
			this.algorithmType = Method.STARTSMART.value;
		}
		
		//Create new CarData object
		carData = new CarData(
				getName().toString(),  // Id
				currentStep.getOriginX(),  // X
				currentStep.getOriginY(),  // Y
				Math.min(getMaxSpeed()/4, // CurrentSpeed at the beginning
						getCurrentSegment().getCurrentAllowedSpeed()), 
				this.algorithmType,
				0,   // Segment distance covered
				this.ratio, this.tini, //Ratio and initialTick
				this.tini // Current tick
				);
		
		if (useLog) {
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
		
		if(this.drawGUI){
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
			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.addReceiver(interfaceAgent.getName());
			msg.setContent(this.carData.toJSON().toString());
			msg.setOntology("newCarOntology");
			send(msg);
		}
		//Register
	    //Add the tini of the first segment
	    this.sensorTrafficData.setTini(this.tini);
	    
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setOntology("carToSegmentOntology");
		msg.setConversationId("register");
		msg.addReceiver(currentStep.getSegment().getSegmentAgent().getAID());
/*		JSONObject carDataRegister = new JSONObject();
		carDataRegister.put("id", getId());
		carDataRegister.put("x", getX());
		carDataRegister.put("y", getY());
		carDataRegister.put("specialColor", getSpecialColor());
		carDataRegister.put("radio", getRatio());
		carDataRegister.put("tickInitial", this.sensorTrafficData.getTini());
		carDataRegister.put("tickFinal", 1001001);*/
		
		msg.setContent(carData.toJSON().toString());
		
		send(msg);
		// Receive the current traffic density from the current 
		//    segment
		msg = blockingReceive(MessageTemplate.
				             MatchOntology("trafficDensityOntology"));
		JSONObject densityData = new JSONObject(msg.getContent());
		setCurrentTrafficDensity(densityData.getDouble("density"));
		
		//Runs the agent
		addBehaviour(new CarBehaviour(this, 50, this.drawGUI));
		addBehaviour(new CarReceivingDataBehaviour(this));

	}
	
	/**
	 * Recalculate the route, this will be called from the behaviour 
	 *     if we are smart.
	 * 
	 * @param origin ID of the intersection where the car is
	 */
	public void recalculate(String origin) {
		
		// A JGraph envision structure must be obteined from jgraphsT 
		//     received by other cars in the twin segment of the 
		//     current segment where the car is going.
		// TODO:
		this.path = getPathOnMethod(origin, finalIntersection);
	}

	//Setters and getters
	public int getDirection() {
		return direction;
	}

	public CarData getCarData() {
		return carData;
	}

	public void setCarData(CarData carData) {
		this.carData = carData;
	}
	
	public float getCurrentPk() {
		return currentPk;
	}

	public void setCurrentPk(float currentPk) {
		this.currentPk = currentPk;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

	public int getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(int maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	public DFAgentDescription getInterfaceAgent() {
		return interfaceAgent;
	}

	public Map getMap() {
		return map;
	}

	public Path getPath() {
		return path;
	}

	public Segment getCurrentSegment() {
		return currentSegment;
	}

	public void setCurrentSegment(Segment previousSegment) {
		this.currentSegment = previousSegment;
	}

	public String getInitialIntersection() {
		return initialIntersection;
	}

	public String getFinalIntersection() {
		return finalIntersection;
	}
	
	public DirectedWeightedMultigraph<Intersection, Edge> 
	                                                   getJgrapht() {
		return jgrapht;
	}

	public void setjgrapht(
			DirectedWeightedMultigraph<Intersection,Edge> jgrapht){
		this.jgrapht = jgrapht;
	}
	
	public boolean isSmart() {
		
		return this.smart;
	}

	public int getAlgorithmType() {
		return algorithmType;
	}	
	
	public int getRatio() {
		return ratio;
	}

	public void setRatio(int ratio) {
		this.ratio = ratio;
	}
	
	public double getCurrentTrafficDensity() {
		return currentTrafficDensity;
	}

	public void setCurrentTrafficDensity(double currentTD) {
		this.currentTrafficDensity = currentTD;
	}

	public long getTini() {
		return tini;
	}

	public void setTini(long tini) {
		this.tini = tini;
	}
	
	public boolean getUseLog(){
		return useLog;
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
	
	public long getCurrentTick() {
		return currentTick;
	}

	public void setCurrentTick(long currentTick) {
		this.currentTick = currentTick;
	}

	public DFAgentDescription getLogAgent() {
		return logAgent;
	}

	public void setLogAgent(DFAgentDescription logAgent) {
		this.logAgent = logAgent;
	}
	
	public List<LogData> getLogData() {
		return logData;
	}

	public void setLogData(List<LogData> logData) {
		this.logData = logData;
	}

	public long getLogInitialTick() {
		return logInitialTick;
	}

	public void setLogInitialTick(long logInitialTick) {
		this.logInitialTick = logInitialTick;
	}

	public long getLogEndTick() {
		return logEndTick;
	}

	public void setLogEndTick(long logEndTick) {
		this.logEndTick = logEndTick;
	}

	public String getLogAlgorithm() {
		return logAlgorithm;
	}

	public void setLogAlgorithm(String logAlgorithm) {
		this.logAlgorithm = logAlgorithm;
	}
	
	public int getNumMsgRecibido() {
		return numMsgRecibido;
	}

	public void setNumMsgRecibido(int numMsgRecibido) {
		this.numMsgRecibido = numMsgRecibido;
	}

	public int getNumMsgEnviados() {
		return numMsgEnviados;
	}

	public void setNumMsgEnviados(int numMsgEnviados) {
		this.numMsgEnviados = numMsgEnviados;
	}

	public void addLogData(String idSegment, int numMsgRecibido, int numMsgEnviados, float distSegment, float velMedia){
		this.logData.add(new LogData(idSegment,numMsgRecibido,numMsgEnviados,distSegment,velMedia));
	}
	
	// TODO: Cambiar este método pata implementar los de hay en algorithms
	public Path getPathOnMethod(String initialInterseccion,
            String finalIntersection) {

		GraphPath<Intersection, Edge> pathGrapht = null;
		if (algorithmType == Method.DYNAMICSMART.value || 
				algorithmType == Method.STARTSMART.value) {
			pathGrapht = DijkstraShortestPath.findPathBetween(jgrapht, 
					map.getIntersectionByID(initialInterseccion),
					map.getIntersectionByID(finalIntersection));
		} else if (algorithmType == Method.SHORTEST.value) {
			@SuppressWarnings("unchecked")
			DirectedWeightedMultigraph<Intersection, Edge> jgraphtClone = 
			(DirectedWeightedMultigraph<Intersection, Edge>) jgrapht.clone();
			putWeightsAsDistancesOnGraph(jgraphtClone);
			pathGrapht = DijkstraShortestPath.findPathBetween(jgraphtClone, 
					map.getIntersectionByID(initialInterseccion),
					map.getIntersectionByID(finalIntersection));
		} else if (algorithmType == Method.FASTEST.value) {
			@SuppressWarnings("unchecked")
			DirectedWeightedMultigraph<Intersection, Edge> jgraphtClone = 
			(DirectedWeightedMultigraph<Intersection, Edge>) jgrapht.clone();
			putWeightAsTripMaxSpeedOnGraph(jgraphtClone);
			pathGrapht = DijkstraShortestPath.findPathBetween(jgraphtClone, 
					map.getIntersectionByID(initialInterseccion),
					map.getIntersectionByID(finalIntersection));
		}
		
		System.out.println("//////////////////// PATH /////////////////");
		System.out.println(pathGrapht.toString());
		System.out.println(pathGrapht);
		System.out.println("//////////////////// PATH 2 /////////////////");
		System.out.println(pathGrapht.getVertexList().toString());		
		System.out.println("//////////////////// END PATH /////////////////");


		List<Step> steps = new ArrayList<Step>();
		List<Segment> segments = new ArrayList<Segment>();
		
		for(Edge e: pathGrapht.getEdgeList()){
			steps.addAll(map.getSegmentByID(e.getIdSegment()).getSteps());
			segments.add(map.getSegmentByID(e.getIdSegment()));
		}
		return new Path(pathGrapht.getVertexList(),
				steps, segments);
	}

	//Used with the shortest method. The road speed is not important
	private void putWeightsAsDistancesOnGraph(
			DirectedWeightedMultigraph<Intersection, Edge> jgrapht2) {
		for(Edge e: jgrapht2.edgeSet()) {
			jgrapht2.setEdgeWeight(e, e.getWeight());
		}
	}
	
	//Used with the fastest method. The fast method depends of the distance and the speed
	private void putWeightAsTripMaxSpeedOnGraph(
			DirectedWeightedMultigraph<Intersection, Edge> jgraphtClone) {
		for(Edge e: jgraphtClone.edgeSet()) {
			jgraphtClone.setEdgeWeight(e, e.getWeight() /
					e.getMaxSpeed());
		}
	}

	private class LogData{
		private String idSegment;
		private int numMsgRecibido;
		private int numMsgEnviados;
		private float distSegment;
		private float velMedia;
		
		public LogData(String idSegment, int numMsgRecibido, int numMsgEnviados, float distSegment, float velMedia) {
			super();
			this.idSegment = idSegment;
			this.numMsgRecibido = numMsgRecibido;
			this.numMsgEnviados = numMsgEnviados;
			this.distSegment = distSegment;
			this.velMedia = velMedia;
		}

		public String getIdSegment() {
			return idSegment;
		}

		public void setIdSegment(String idSegment) {
			this.idSegment = idSegment;
		}

		public int getNumMsgRecibido() {
			return numMsgRecibido;
		}

		public void setNumMsgRecibido(int numMsgRecibido) {
			this.numMsgRecibido = numMsgRecibido;
		}

		public int getNumMsgEnviados() {
			return numMsgEnviados;
		}

		public void setNumMsgEnviados(int numMsgEnviados) {
			this.numMsgEnviados = numMsgEnviados;
		}

		public float getDistSegment() {
			return distSegment;
		}

		public void setDistSegment(float distSegment) {
			this.distSegment = distSegment;
		}

		public float getVelMedia() {
			return velMedia;
		}

		public void setVelMedia(float velMedia) {
			this.velMedia = velMedia;
		}

		@Override
		public String toString() {
			return idSegment + "," + numMsgRecibido + ","+ numMsgEnviados + "," + distSegment + "," + velMedia;
		}
		
	}

}
