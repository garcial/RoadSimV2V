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

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import agents.CarAgent.BlockData;
import agents.CarAgent.TrafficData;
import behaviours.CarBehaviour;
import behaviours.CarReceivingDataBehaviour;
import environment.Map;
import environment.Path;
import environment.Segment;
import environment.Step;

/**
 * This code represents a mobile car, it will have an origin an a 
 * destination and will get there using either the shortest, 
 * fastest or smartest path.
 *
 */
public class CarAgent extends Agent {


	private static final long serialVersionUID = 1L;

	public static final int MAXWORLDX = 800;
	public static final int MAXWORLDY = 695;

	private float x, y;
	private int direction;
	private int ratio;
	private int currentSpeed,maxSpeed;
	private double currentTrafficDensity;
	private String id; 
	private DFAgentDescription interfaceAgent;
	private Map map;
	private Path path;
	private Segment currentSegment;
	private String initialIntersection, finalIntersection;
	private boolean specialColor = false;
	private boolean smart = false;
	private Algorithm alg;
	private int algorithmType;

	// future: is for storing data received from other cars and
	//    used for computing my route to destination
	private TrafficData futureTraffic;

	// past: is for informing data to send to other cars about
	//    what is the traffic state in my performed route
    private TrafficData pastTraffic;

	public TrafficData getPastTraffic() {
		return pastTraffic;
	}

	public void setPastTraffic(TrafficData pastTraffic) {
		this.pastTraffic = pastTraffic;
	}

	public TrafficData getFutureTraffic() {
		return futureTraffic;
	}

	public void setFutureTraffic(TrafficData futureTraffic) {
		this.futureTraffic = futureTraffic;
	}
	
	

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
		
		//Get the starting and final points of my trip
		this.initialIntersection = (String) this.getArguments()[1];
		this.finalIntersection = (String) this.getArguments()[2];
		
		//Get the speeds
		this.maxSpeed = (int) this.getArguments()[3];
		this.currentSpeed = 0; //Se gestiona en el comportamiento 
		                       // (int) this.getArguments()[4];

		//Get the method we want
		AlgorithmFactory factory = new AlgorithmFactory();
		this.alg = null;
		
		String routeType = (String) this.getArguments()[4];
		
		if (routeType.equals("fastest")) {
			
			this.algorithmType = Method.FASTEST.value;
			this.alg = factory.getAlgorithm(Method.FASTEST);
			
		} else if (routeType.equals("shortest")) {
			 
			this.algorithmType = Method.SHORTEST.value;
			this.alg = factory.getAlgorithm(Method.SHORTEST);
			
		} else if (routeType.equals("smartest")) {
			
			this.algorithmType = Method.SMARTEST.value;
			this.alg = factory.getAlgorithm(Method.SMARTEST);
			this.smart = true;
		}
		
		//Get the desired Path from the origin to the destination
		this.path = alg.getPath(this.map, getInitialIntersection(), 
				                getFinalIntersection(), this.maxSpeed);
		
		//Starting point
		setX(map.getIntersectionByID(getInitialIntersection()).getX());
		setY(map.getIntersectionByID(getInitialIntersection()).getY());
		
		//Store data received from other cars in a Map
		futureTraffic = new TrafficData();
		
		//Store data to send to other cars in my route
		pastTraffic = new TrafficData();

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

		//An unique identifier for the car
		this.id = getName().toString();

		//We notify the interface about the new car
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.addReceiver(interfaceAgent.getName());
		JSONObject carData = new JSONObject();
		carData.put("x", this.x);
		carData.put("y", this.y);
		carData.put("id", this.id);
		carData.put("algorithmType", this.algorithmType);
		msg.setContent(carData.toString());
		msg.setOntology("newCarOntology");
		send(msg);

		// Set the initial values for the carAgent on the road
		Step next = getPath().getGraphicalPath().get(0);
	    setCurrentSegment(next.getSegment());

		//Register
		msg = new ACLMessage(ACLMessage.INFORM);
		msg.setOntology("carToSegmentOntology");
		msg.setConversationId("register");
		msg.addReceiver(next.getSegment().getSegmentAgent().getAID());
		JSONObject carDataRegister = new JSONObject();
		carDataRegister.put("id", getId());
		carDataRegister.put("x", getX());
		carDataRegister.put("y", getY());
		carDataRegister.put("specialColor", getSpecialColor());
		carDataRegister.put("radio", getRatio());
		
		msg.setContent(carDataRegister.toString());
		
		send(msg);
		// Receive the current traffic density from the current segment
		msg = blockingReceive(MessageTemplate.
				              MatchOntology("trafficDensityOntology"));
		
		JSONObject densityData = new JSONObject(msg.getContent());
		
		setCurrentTrafficDensity(densityData.getDouble("density"));
		//Change my speed according to the maximum allowed speed
	    setCurrentSpeed(Math.min(getMaxSpeed(), 
	    		         getCurrentSegment().getCurrentAllowedSpeed()));
			
	    //If we are going under the maximum speed I'm allowed to go, 
	    //  or I can go, I am in a congestion, draw me differently
	    if (getCurrentSpeed() < Math.min(this.getMaxSpeed(), 
	    		             this.getCurrentSegment().getMaxSpeed())) {

	    	setSpecialColor(true);
	    } else {

	    	setSpecialColor(false);
	    }

		//Runs the agent
		addBehaviour(new CarBehaviour(this, 50));	
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
		this.path = this.alg.getPath(this.map, origin, 
				               getFinalIntersection(), this.maxSpeed);
	}

	//Setters and getters
	public int getDirection() {
		return direction;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

	public int getCurrentSpeed() {
		return currentSpeed;
	}

	public void setCurrentSpeed(int currentSpeed) {
		this.currentSpeed = currentSpeed;
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

	public String getId() {
		return id;
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

	public boolean getSpecialColor() {
		return specialColor;
	}

	public void setSpecialColor(boolean specialColor) {
		this.specialColor = specialColor;
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

	public void setCurrentTrafficDensity(double currentTrafficDensity) {
		this.currentTrafficDensity = currentTrafficDensity;
	}


	public class TrafficData {
		private HashMap<String, ArrayList<BlockData>> data;
		
		public TrafficData() {
			data = new HashMap<String, ArrayList<BlockData>>();
		}

		public HashMap<String, ArrayList<BlockData>> getData() {
			return data;
		}

		public void setData(HashMap<String, ArrayList<BlockData>> data) {
			this.data = data;
		}
		public void put(String key, JSONObject json) {
			if (data.containsKey(key)) data.get(key).add(new BlockData(json));
			else {
				ArrayList<BlockData> al = new ArrayList<BlockData>();
				al.add(new BlockData(json));
				data.put(key, al);
			}
		}
		
	}
	
	public class BlockData {
		private int tini;
		private int tfin;
		private int numCars;
		private ArrayList<Double> carsPositions;
		private ArrayList<Double> carsSpeeds;

		public BlockData(int tini, int tfin, int numCars,
				         ArrayList<Double> carsPositions, 
				         ArrayList<Double> carsSpeeds) {
			super();
			this.tini = tini;
			this.tfin = tfin;
			this.setNumCars(numCars);
			this.carsPositions = carsPositions;
			this.carsSpeeds = carsSpeeds;
		}
		
		public BlockData(JSONObject json) {
			this.tini = json.getInt("tini");
			this.tfin = json.getInt("tfin");
			this.setNumCars(json.getInt("numCars"));
			JSONArray carPos = json.getJSONArray("positions");
			this.carsPositions = new ArrayList<Double>();
			for(int i = 0; i<carPos.length(); i++) 
				carsPositions.add(carPos.getDouble(i));
			JSONArray speeds = json.getJSONArray("speeds");
			this.carsSpeeds = new ArrayList<Double>();
			for(int i = 0; i<speeds.length(); i++)
				carsSpeeds.add(speeds.getDouble(i));			
		}
		
		public int getTini() {
			return tini;
		}
		public void setTini(int tini) {
			this.tini = tini;
		}
		public int getTfin() {
			return tfin;
		}
		public void setTfin(int tfin) {
			this.tfin = tfin;
		}
		public ArrayList<Double> getCarsPositions() {
			return carsPositions;
		}
		public void setCarsPositions(ArrayList<Double> carsPositions) {
			this.carsPositions = carsPositions;
		}
		public ArrayList<Double> getCarsSpeeds() {
			return carsSpeeds;
		}
		public void setCarsSpeeds(ArrayList<Double> carsSpeeds) {
			this.carsSpeeds = carsSpeeds;
		}

		public int getNumCars() {
			return numCars;
		}

		public void setNumCars(int numCars) {
			this.numCars = numCars;
		}
		
	}


}
