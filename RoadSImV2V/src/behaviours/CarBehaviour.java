package behaviours;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.ToJSON;
import agents.CarAgent;
import environment.Segment;
import environment.Step;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import trafficData.TrafficData;
import trafficData.TrafficDataOutStore;
import jgrapht.Edge;

/**
 * This behaviour is used by the CarAgent and calculates the next 
 * graphical position of the car. It also registers and deregisters
 * the car from the segments.
 * 
 * The car is registered when it enters a new segment and deregistered
 * when it leaves a segment.
 *
 */
public class CarBehaviour extends CyclicBehaviour {

	private CarAgent agent;
	private AID topic;
	private boolean done = false;
	private char serviceLevelSegment;
	private boolean drawGUI;
	private long previousTick;
	private long currentTick;
	private float currentSegmentCovered=0;
	//For recording routing total time
	private long tstart;
	//For recording how many km has been covered yet of the current
	// Step
	private float stepDistanceCovered;

	public CarBehaviour(CarAgent a, long timeout, boolean drawGUI) {

		this.agent = a;
		this.drawGUI = drawGUI;
		this.topic = null;
		previousTick = agent.getTini() - 1;
		this.tstart = agent.getTini();
		this.stepDistanceCovered = 0f;
		
		try {
			TopicManagementHelper topicHelper =(TopicManagementHelper) 
				this.agent.getHelper(TopicManagementHelper.
						                                SERVICE_NAME);
			topic = topicHelper.createTopic("tick");
			topicHelper.register(topic);
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static final long serialVersionUID = 1L;

	@Override
	public void action() {

		//Block until tick is received
		ACLMessage msg = 
				myAgent.receive(MessageTemplate.MatchTopic(topic));

		if (msg != null) {
			
			this.currentTick = Long.parseLong(msg.getContent());
			this.agent.setCurrentTick(currentTick);
		    // Increase elapsed time
			//agent.increaseElapsedtime();
			//If I still have to move somewhere
			if(this.agent.getPath().getGraphicalPath().size() > 0) {
				//Get the path
				Step currentStep = this.agent.getPath().getGraphicalPath().
						                         get(0);
				// First calculate the currentSpeed,Greenshield model
				int currentSpeed = (int) Math.min(
				         agent.getMaxSpeed(),
				         agent.getCurrentSegment().getMaxSpeed() *
	                     (1-agent.getCurrentTrafficDensity()/28.2));
				
				agent.setCurrentSpeed(currentSpeed);
				
				float currentPk = this.agent.getCurrentPk();
				//TODO: Revise formula to compute pkIncrement
				float deltaTime = (currentTick - previousTick) / 3600f;
				float deltaPk = (float) currentSpeed * deltaTime;
				
				this.currentSegmentCovered += deltaPk;
/*				System.out.println("CurrentTick: " + currentTick);
				System.out.println("PreviousTick: " + previousTick);
				System.out.println("DeltaTimeManual: " + (currentTick - previousTick) / 3600f);
				System.out.println("DeltaTime: " + deltaTime);
				System.out.println("PKCurrent: " + deltaPk);*/
				
				// Update pkCurrent with this speed and the difference
				//   between previousTick and currentTick
				//   We transform km/h to k/s if divide it by 3600
		
				//Virtual position
				float currentX = this.agent.getX();
				float currentY = this.agent.getY();
				
				//Update step distance covered
				this.stepDistanceCovered += deltaPk;

				//Check if we need to go to the next step
				/*System.out.println("StepDistanceCovered:" + this.stepDistanceCovered);
				System.out.println("Longitud del step: " + currentStep.getStepLength());*/
				while (stepDistanceCovered > 
			            currentStep.getStepLength()) {
					stepDistanceCovered -= currentStep.getStepLength();
					//If there is still a node to go
					if (this.agent.getPath().
							getGraphicalPath().size()> 1) {
						//Remove the already run path
						this.agent.getPath().getGraphicalPath().
						                     remove(0);
						currentStep = this.agent.getPath().
								getGraphicalPath().get(0);
						System.out.println("CAMBIO DE STEP");
						currentX = currentStep.getOriginX();
						currentY = currentStep.getOriginY();				
					} else {
						this.kill();
						break;
					}
				}

				if (!this.done) {
					
					//Proportion inside the segment
					float proportion = deltaPk *  stepDistanceCovered / 
					           currentStep.getStepLength();
					
					
					//Update the current pk when update the x and y
					if("up".compareTo(this.agent.getCurrentSegment().getDirection()) == 0){
						this.agent.setCurrentPk(currentPk + stepDistanceCovered);
						previousTick = this.agent.getCurrentTick() - 1;
					} else {
						this.agent.setCurrentPk(currentPk - stepDistanceCovered);
						previousTick = this.agent.getCurrentTick() - 1;
					}
					System.out.println("Proporción: " + proportion);
					System.out.println("X: " + ((1 - proportion) * currentX + proportion * currentStep.getDestinationX()) + " - Y: " + ((1 - proportion) * currentY + proportion * currentStep.getDestinationY()));
					this.agent.setX(((1 - proportion) * currentX + 
							proportion * currentStep.getDestinationX()));
					this.agent.setY(((1 - proportion) * currentY + 
							proportion * currentStep.getDestinationY()));

					//If I am in a new segment
					if (!this.agent.getCurrentSegment().
							equals(currentStep.getSegment())) {

						long tfin = Long.parseLong(msg.getContent());

						//Deregister from previous segment
						this.informSegment(
							this.agent.getCurrentSegment(), 
							"deregister");

						String previousSegmentId = agent.
								         getCurrentSegment().getId();
						//Set the new previous segment
						this.agent.
						        setCurrentSegment(currentStep.getSegment());

						//Register in the new segment
						this.informSegment(currentStep.getSegment(),
								           "register");
						
						//Calculate de information to remove the 
						//   segment that you register
						agent.setTini(tfin);
						agent.setCurrentPk(currentStep.getSegment().getPkIni());
						//I don't know if remove the edge or if remove
						//   the content of the edge
						this.agent.getJgraht().removeEdge(
								currentStep.getSegment().getOrigin(), 
								currentStep.getSegment().getDestination());
						
						// TODO:If we are using the smart algorithm, 
						//  recalculate all the traffic states on the 
						//  map with the information provided from 
						//  othercarAgents, and then rerouting 
						//  accordingly.
						// TODO: futureTrafficStore analysis

						this.agent.setCurrentPk(currentStep.getSegment().getPkIni());
						if("up".compareTo(this.agent.getCurrentSegment().
								getDirection()) == 0){
							this.agent.setCurrentPk(currentPk + stepDistanceCovered);
						} else {
							this.agent.setCurrentPk(currentPk - stepDistanceCovered);
						}
						
						if (this.agent.isSmart()) {
						    this.agent.recalculate(
								this.agent.getCurrentSegment().
								           getOrigin().getId());
						}
						
						// Once rerouted, Delete data from future 
						//     Traffic related to this new segment
						agent.getFutureTraffic().
						           delete(currentStep.getSegment().getId());
						//agent.getPastTraffic().put(previousSegmentId, 
						//		        agent.getSensorTrafficData());
					}
					
					//If we are going under the maximum speed I'm 
					//   allowed to go, or I can go, I am in a 
					//   congestion, draw me differently
					//I don't know if is necessary here but i 
					//   change this in the destination
					if(this.drawGUI){
						if (this.agent.getCurrentSpeed() < 
								Math.min(
								  this.agent.getMaxSpeed(),
								  this.agent.getCurrentSegment().
								             getMaxSpeed())) {
							
							this.agent.setSpecialColor(true);
						} else {
							
							this.agent.setSpecialColor(false);
						}
					}

					this.informSegment(currentStep.getSegment(), "update");
					previousTick = currentTick;
					agent.addBehaviour(
							    new CarSendingDataBehaviour(agent));
				}
			}
		} else block();
	}

	//This method will send a message to a given segment
	private void informSegment(Segment segment, String type) {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setOntology("carToSegmentOntology");
		msg.setConversationId(type);
		
		//TODO ACtualizar el pastTraficData y el futureData además del JGrapht
		if ("deregister".compareTo(type) == 0) {
			// Introducir el Tfin en TrafficData
			this.agent.getSensorTrafficData().setTfin(this.currentTick);
			// First give the number of cars detected
			agent.getSensorTrafficData().
			        setNumCars(agent.
			        	    getSensorTrafficData().
			        	    getCarsPositions().size());
			
			this.agent.getPastTraffic().put(segment.getId(), this.agent.getSensorTrafficData());
			/*System.out.println("PAST TRAFFIC de " + this.agent.getId());
			for (String key : this.agent.getPastTraffic().getData().keySet()){
				System.out.println("CBinfSeg: " + key + " - " +this.agent.getPastTraffic().getData().get(key));
			}
			System.out.println("Distancia: " + (float) segment.getLength() * 1000);
			System.out.println("Distancia real: " + (float) this.currentSegmentCovered);
			System.out.println("Tiempo: " + this.agent.getSensorTrafficData().getTfin() + " - " + this.agent.getSensorTrafficData().getTini() );*/
			float vel = (((float) this.currentSegmentCovered) / (this.agent.getSensorTrafficData().getTfin() - this.agent.getSensorTrafficData().getTini()) * 3600);
			this.agent.addLogData(segment.getId(), this.agent.getNumMsgRecibido(), this.agent.getNumMsgEnviados(), (float) segment.getLength(), vel);
			//We reinicializate the number og messages
			this.agent.setNumMsgEnviados(0);
			this.agent.setNumMsgRecibido(0);
			
			//Update the EDGE
			/*Es decir tenemos que recorrer toda la lista buscando
			 * el que tenga la diferencia de tiempos mayor y a partir de 
			 * ahí generar un edge y guardarlo en el jgrapht.*/
			//Calculate the better way
		}else if("register".compareTo(type) == 0){
			//Start a new current trafficData by myself
			this.currentSegmentCovered = 0;
			agent.setSensorTrafficData(new TrafficData());
			agent.getSensorTrafficData().setTini(this.currentTick);
			//To log the info segment we need the initial tick
		} else if("update".compareTo(type) == 0){
			
		}
		
		msg.addReceiver(segment.getSegmentAgent().getAID());
		JSONObject carDataRegister = new JSONObject();
		carDataRegister.put("tickInitial", this.agent.getSensorTrafficData().getTini());
		carDataRegister.put("tickFinal", this.currentTick);
		carDataRegister.put("id", this.agent.getId());
		carDataRegister.put("x", this.agent.getX());
		carDataRegister.put("y", this.agent.getY());
		carDataRegister.put("specialColor", 
				            this.agent.getSpecialColor());
		carDataRegister.put("radio", this.agent.getRatio());
		
		msg.setContent(carDataRegister.toString());
		myAgent.send(msg);
	}

	public void kill() {

		//Done flag
		this.done = true;
		//Deregister from previous segment
		this.informSegment(this.agent.getCurrentSegment(),
				           "deregister");
		this.agent.setLogEndTick(this.currentTick);
		ACLMessage msgLog = new ACLMessage(ACLMessage.INFORM);
		msgLog.setOntology("logCarOntology");
		msgLog.addReceiver(this.agent.getLogAgent().getName());
		msgLog.setContent(this.agent.getLogData().toString() + "," +
		this.agent.getLogInitialTick() + "," + this.agent.getLogEndTick() +
		"," + this.agent.getLogAlgorithm());
		myAgent.send(msgLog);

		//Delete the car from the canvas
		if (this.agent.getInterfaceAgent() != null && this.drawGUI) {

			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.setOntology("deleteCarOntology");
			msg.addReceiver(this.agent.getInterfaceAgent().getName());
			msg.setContent(ToJSON.toJSon("id",this.agent.getId()));

			myAgent.send(msg);
		}
		
		//Deregister the agent
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(this.agent.getAID());
		
		try {
			DFService.deregister(this.agent,  dfd);
		} catch (Exception e) { 
		}

		this.agent.doDelete();
	}
}