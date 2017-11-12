package behaviours;

import org.json.JSONObject;

import agents.SegmentAgent;
import environment.Segment;
import features.CarData;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * This behaviour is used by the SegmentAgent and listens to messages
 * either by cars to register, deregister or update themselves from it
 * or from the EventManagerAgent to tell them updates on its status.
 *
 */
public class SegmentListenBehaviour extends Behaviour {

	private static final long serialVersionUID =-2533061568306629976L;

	//Template to listen for the new communications from cars
	private MessageTemplate mtCarControl = 
			MessageTemplate.and(
			  MessageTemplate.MatchPerformative(ACLMessage.INFORM),
			  MessageTemplate.MatchOntology("carToSegmentOntology"));

	private MessageTemplate mtEventManagerControl = 
		 MessageTemplate.and(
		   MessageTemplate.MatchPerformative(ACLMessage.INFORM),
		   MessageTemplate.MatchOntology(
				                   "eventManagerToSegmentOntology"));

	private MessageTemplate mt =
             MessageTemplate.or(mtCarControl, mtEventManagerControl);

	private SegmentAgent mySegmentAgent;
	private int previousServiceLevel = 0;

	//Constructor
	public SegmentListenBehaviour(SegmentAgent agent) {
		this.previousServiceLevel = 0;
		this.mySegmentAgent = agent;
	}

	@Override
	public void action() {

		ACLMessage msg = myAgent.receive(mt);

		if (msg != null) { //There is a message
			
			if (msg.getOntology().equals("carToSegmentOntology")) {

				JSONObject car = new JSONObject(msg.getContent());

				//Register
				if (msg.getConversationId().equals("update")) { 
					//Update position
					this.mySegmentAgent.updateCar(car);
				} else {
					if (msg.getConversationId().equals("register")) {
						// Register
						this.mySegmentAgent.addCar(car);							
					} else {    
						// Deregister  of the segment
						this.mySegmentAgent.removeCar(car.getString("id"));
					}
					
					Segment segment = this.mySegmentAgent.getSegment();
					int numCars = this.mySegmentAgent.getCars().size();
					
					//Set the density
					double density = numCars/segment.getLength();
					segment.setDensity(density);
					
					//Set the service level
					int currentSL;
					if (density < 11) {						
						currentSL = 0; // 'A';
					} else if (density < 18) {
						currentSL = 1; // 'B';
					} else if (density < 26) {
						currentSL = 2; // 'C';
					} else if (density < 35) {
						currentSL = 3; // 'D';
					} else if (density < 43) {
						currentSL = 4; // 'E';
					} else 
						currentSL = 5; // 'F';
					
					// Tell all cars into this segment how many cars each one
					//   has ahead of its current position
					ACLMessage msg2 = new ACLMessage(ACLMessage.INFORM);
					msg2.setOntology("trafficCarsAheadOntology");

					JSONObject densityData = new JSONObject();
					if (msg.getConversationId().equals("register")) {
						
						densityData.put("type", "register");
						densityData.put("cars", mySegmentAgent.getCars().size() - 1);
						msg2.addReceiver(msg.getSender());
					} else { //Inform all other cars there is one less car 
						     //   running into this segment
						densityData.put("type", "deregister");
						densityData.put("cars", -1);
						for (String id:mySegmentAgent.getCars().keySet()) {
							msg2.addReceiver(new AID(id, true));;			
						}
					}

					msg2.setContent(densityData.toString());
					mySegmentAgent.send(msg2);	
					
					//TODO: Añadir una variable de nivel de servicio anterior
					//  y así guardamos el tickfinal. 
					//  Hay que ver donde se recoge el tick
					if(segment.getCurrentServiceLevel() != 
					   this.mySegmentAgent.getServiceLevelPast() && 
					   !msg.getConversationId().equals("register")) {
						ACLMessage msgLog = new ACLMessage(ACLMessage.INFORM);
						msgLog.setOntology("logSegmentOntology");
						msgLog.addReceiver(this.mySegmentAgent.getLogAgent().getName());
						msgLog.setContent(segment.getId() + "," + 
						                  segment.getCurrentServiceLevel() + 
						                  "," + car.getLong("initialTick") + 
						                  "," +car.getLong("tick"));
						myAgent.send(msgLog);
						
						this.mySegmentAgent.setServiceLevelPast(
								segment.getCurrentServiceLevel());
					}
					
					if (currentSL != previousServiceLevel) {
						//Store current data of the edge in the list of 
						// previous data
						
						//Start computing the average speed
						double averageSpeed = 0.0;
						for(CarData cd:mySegmentAgent.getCars().values()) {
							averageSpeed += cd.getCurrentSpeed();
						}
						averageSpeed = averageSpeed / numCars;
						
						mySegmentAgent.setTini(car.getLong("tick"));

						previousServiceLevel = currentSL;
						mySegmentAgent.getSegment().setCurrentServiceLevel(currentSL);
					}
					
				}

				
			} else if (msg.getOntology().
					        equals("eventManagerToSegmentOntology")) {
				
				Segment segment = this.mySegmentAgent.getSegment();
				// TODO Service level era un char pero ahora en teoria no es así
				char serviceLevel = msg.getContent().charAt(0);
				
				segment.setCurrentServiceLevel(serviceLevel);
			}
			
		} else block();
	}


	@Override
	public boolean done() {

		return false;
	}
}
