package behaviours;
import org.json.JSONObject;
import org.json.ToJSON;
import agents.CarAgent;
import environment.Segment;
import environment.Step;
import graph.Edge;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import trafficData.TrafficData;

/**
 * This behaviour is used by the CarAgent and calculates the next 
 * graphical position of the car. It also registers and deregisters
 * the car from the segmentsTest.
 * 
 * The car is registered when it enters a new segment and deregistered
 * when it leaves a segment.
 *
 */
public class CarBehaviour extends CyclicBehaviour {

	private CarAgent agent;
	private AID topic;
	private boolean done = false;
	private boolean drawGUI;
	private long previousTick;
	private long currentTick;
	//For recording how many km has been covered yet of the current
	private float currentSegmentCovered=0;
	// Step
	private float stepDistanceCovered;


	public CarBehaviour(CarAgent a, long timeout, boolean drawGUI) {

		this.agent = a;
		this.drawGUI = drawGUI;
		this.topic = null;
		previousTick = agent.getTini() - 1;
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
		ACLMessage msg = myAgent.receive(MessageTemplate.MatchTopic(topic));

		if (msg != null) {
			
			this.currentTick = Long.parseLong(msg.getContent());
			this.agent.getCarData().setCurrentTick(currentTick);

			//If I still have to move somewhere
			if(this.agent.getPath().getGraphicalPath().size() > 0) {
				//Get the path
				Step currentStep = this.agent.getPath().
								   getGraphicalPath().get(0);
				
				// First calculate the currentSpeed,Greenshield model
				int currentSpeed = (int) Math.min(
						 this.agent.getMaxSpeed(),
					     (this.agent.getCurrentTrafficDensity() >= 43f)? 5:
					     this.agent.getCurrentSegment().getMaxSpeed() *
		                 (1-this.agent.getCurrentTrafficDensity()/43f));
				
				//TODO: Delete the agent variables to export it to carData
				//agent.setCurrentSpeed(currentSpeed);
				agent.getCarData().setCurrentSpeed(currentSpeed);
				
				float currentPk = this.agent.getCurrentPk();
				//TODO: Revise formula to compute pkIncrement
				float deltaTime = (currentTick - previousTick) / 3600f;
				float deltaPk = (float) currentSpeed * deltaTime;
				//System.out.println("PK current: " + currentPk);
				
				float graphCovered = deltaPk * 
				        currentStep.getStepGraphicalLength() /
				        currentStep.getStepLength();
				
				this.currentSegmentCovered += deltaPk;
				
				// Update pkCurrent with this speed and the difference
				//   between previousTick and currentTick
				//   We transform km/h to k/s if divide it by 3600
		
				//Virtual position
				float currentX = this.agent.getCarData().getX();
				float currentY = this.agent.getCarData().getY();
				
				//Update step distance covered
				this.stepDistanceCovered += deltaPk;
				this.agent.getCarData().incSegmentDistanceCovered(deltaPk);
				this.agent.getCarData().incTripDistanceCovered(deltaPk);

				//Check if we need to go to the next step
				/*System.out.println("StepDistanceCovered:" + this.stepDistanceCovered);
				System.out.println("Longitud del step: " + currentStep.getStepLength());*/
				while (stepDistanceCovered > currentStep.getStepLength()) {
					stepDistanceCovered -= currentStep.getStepLength();
					//If there is still a node to go
					if (this.agent.getPath().getGraphicalPath().size()> 1) {
						//Remove the already run path
						this.agent.getPath().getGraphicalPath().remove(0);
						currentStep = this.agent.getPath().getGraphicalPath().get(0);
						//System.out.println("CAMBIO DE STEP");
						currentX = currentStep.getOriginX();
						currentY = currentStep.getOriginY();				
					} else {
						this.kill();
						break;
					}
				}

				if (!this.done) {
										
					//Update the current pk when update the x and y
					if("up".compareTo(this.agent.getCurrentSegment().getDirection()) == 0){
						this.agent.setCurrentPk(currentPk + stepDistanceCovered);
					} else {
						this.agent.setCurrentPk(currentPk - stepDistanceCovered);
					}
					
					float proportion = graphCovered / currentStep.getStepGraphicalLength() ;

					this.agent.getCarData().setX(currentX + proportion * 
							(currentStep.getDestinationX() - currentStep.getOriginX()));
					this.agent.getCarData().setY(currentY + proportion * 
							(currentStep.getDestinationY() - currentStep.getOriginY()));

					//If I am in a new segment
					if (!this.agent.getCurrentSegment().equals(currentStep.getSegment())) {
						long tfin = Long.parseLong(msg.getContent());

						//delete the surplus of km added to the previous segment 
						this.agent.getCarData().incSegmentDistanceCovered(-stepDistanceCovered);
						/*System.out.println("CB: " + this.agent.getLocalName() +
								";" + this.agent.getCarData().getSegmentDistanceCovered() +
								";" + this.agent.getCurrentSegment().getLength() +";");*/

						//Deregister from previous segment
						this.informSegment(this.agent.getCurrentSegment(),"deregister");
						
						//Set the new previous segment
						this.agent.setCurrentSegment(currentStep.getSegment());
						
						this.agent.getCarData().setSegmentDistanceCovered(stepDistanceCovered);

						//Register in the new segment
						this.informSegment(currentStep.getSegment(),"register");
						
						//Calculate de information to remove the 
						//   segment that you register
						agent.setTini(tfin);
						
						// TODO:If we are using the smart algorithm, 
						//  recalculate all the traffic states on the 
						//  map with the information provided from 
						//  othercarAgents, and then rerouting 
						//  accordingly.
						// TODO: futureTrafficStore analysis

						this.agent.setCurrentPk(currentStep.getSegment().getPkIni());
						if("up".compareTo(this.agent.getCurrentSegment().getDirection()) == 0){
							this.agent.setCurrentPk(currentPk + stepDistanceCovered);
						} else {
							this.agent.setCurrentPk(currentPk - stepDistanceCovered);
						}
						
						if (this.agent.isSmart()) {
						    try {
								this.agent.recalculate(this.agent.getCurrentSegment().getOrigin().getId());
							} catch (CloneNotSupportedException e) {
								e.printStackTrace();
							}
						}
						
						// Once rerouted, Delete data from future 
						//     Traffic related to this new segment
						agent.getFutureTraffic().delete(currentStep.getSegment().getId());
						//agent.getPastTraffic().put(previousSegmentId,agent.getSensorTrafficData());
					}

					this.informSegment(currentStep.getSegment(), "update");
					previousTick = currentTick;
					agent.addBehaviour(
							    new CarSendingDataBehaviour(agent));
				}
			}
		} else block();
	}
	
	private int calculateServiceLevel(int numCars, double legth){
        int currentSL;
        double density = numCars / legth;
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

        return currentSL;
	}

	//This method will send a message to a given segment
	private void informSegment(Segment segment, String type) {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setOntology("carToSegmentOntology");
		msg.setConversationId(type);
		
		//TODO Ahora se va a desregistrar del segmento. En este momento hay que reconfigurar los
		//TrafficData y poner el grafo como toca.
		if ("deregister".compareTo(type) == 0) {
			// Introducir el Tfin en TrafficData
			this.agent.getSensorTrafficData().setTfin(this.currentTick);
			// First give the number of cars detected
			this.agent.getSensorTrafficData().
			        setNumCars(agent.getSensorTrafficData().
			        	    	getCarsPositions().size());
			
			//System.out.println("CB Pasamos de sensor a past en " + segment.getId() + " de " + this.agent.getName() + " - " + this.agent.getSensorTrafficData() );
			this.agent.getPastTraffic().put(segment.getId(), this.agent.getSensorTrafficData());
			
			//Cambiar el graph
			//System.out.println("Cambiar el grafo de futuro en " + segment.getId());
			for(String seg: this.agent.getFutureTraffic().getData().keySet()){
				Edge edge =  this.agent.getGraph().getEdgeById(seg);
				long tiniAux = edge.getTini();
				long tfinAux = edge.getTfin();
				TrafficData dataAux = null;
				for(TrafficData t : this.agent.getFutureTraffic().getData().get(seg)){
					// TODO Voy a utilizar que el tini haya empezado antes y en el caso de que sean igual el que 
					// tfin sea más grande pasa
					if((t.getTfin() > tfinAux) || (t.getTfin() == tfinAux && t.getTini() < tiniAux) ){
						tiniAux = t.getTini();
						tfinAux = t.getTfin();
						dataAux = t;
					}
				}
				
				if(dataAux != null){
					int serviceLevel = this.calculateServiceLevel(dataAux.getNumCars(), this.agent.getCurrentSegment().getLength());
					// TODO: Aqui el nivel de servicio no se cual poner. Que calculo con el número de coches he de hacer
					
					edge.updateEdge(seg,serviceLevel, this.agent.getCurrentSegment().getLength()/this.agent.getCurrentSegment().getCurrentAllowedSpeed() , this.agent.getCurrentSegment().getMaxSpeed(), dataAux.getTini(), dataAux.getTfin());
				}
				
			}
			//System.out.println(this.agent.getJgrapht().getEdges());
			/*System.out.println("PAST TRAFFIC de " + this.agent.getId());
			for (String key : this.agent.getPastTraffic().getData().keySet()){
				System.out.println("CBinfSeg: " + key + " - " +this.agent.getPastTraffic().getData().get(key));
			}
			System.out.println("Distancia: " + (float) segment.getLength() * 1000);
			System.out.println("Distancia real: " + (float) this.currentSegmentCovered);
			System.out.println("Tiempo: " + this.agent.getSensorTrafficData().getTfin() + " - " + this.agent.getSensorTrafficData().getTini() );*/
			float vel = (((float) this.currentSegmentCovered) / (this.agent.getSensorTrafficData().getTfin() - this.agent.getSensorTrafficData().getTini()) * 3600);
			this.agent.addLogData(segment.getId(), this.agent.getNumMsgRecibido(), this.agent.getNumMsgEnviados(), (float) this.currentSegmentCovered, vel);
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
			//agent.setFutureTraffic(new TrafficDataInStore());
			agent.setSensorTrafficData(new TrafficData());
			agent.getSensorTrafficData().setTini(this.currentTick);
			//To log the info segment we need the initial tick
		} else if("update".compareTo(type) == 0){
			
		}
		
		msg.addReceiver(segment.getSegmentAgent().getAID());
		JSONObject carDataRegister = new JSONObject();
		carDataRegister.put("id", this.agent.getCarData().getId());
		carDataRegister.put("x", this.agent.getCarData().getX());
		carDataRegister.put("y", this.agent.getCarData().getY());
		carDataRegister.put("speed", this.agent.getCarData().getCurrentSpeed());
		carDataRegister.put("type", this.agent.getAlgorithmType());
		carDataRegister.put("segmentDistanceCovered", this.stepDistanceCovered);
		carDataRegister.put("tick", this.currentTick);
		carDataRegister.put("radio", this.agent.getRatio());
		carDataRegister.put("initialTick", this.agent.getTini());
		carDataRegister.put("tripDistanceCovered", this.agent.getCarData().getTripDistanceCovered());
		
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
		/*System.out.println("CB Al acabar tenemos un pasado - id " + this.agent.getName() + " : " + this.agent.getPastTraffic().getData().toString() );
		System.out.println("CB Al acabar tenemos un sensor - id " + this.agent.getName() + " : " + this.agent.getSensorTrafficData() );
		System.out.println("CB Al acabar tenemos un futuro - id " + this.agent.getName() + " : " + this.agent.getFutureTraffic().getData().toString() );*/
		msgLog.setContent(this.agent.getLogData().toString() + "," +
		this.agent.getLogInitialTick() + "," + this.agent.getLogEndTick() +
		"," + this.agent.getLogAlgorithm());
		myAgent.send(msgLog);

		//Delete the car from the canvas
		if (this.agent.getInterfaceAgent() != null && this.drawGUI) {

			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.setOntology("deleteCarOntology");
			msg.addReceiver(this.agent.getInterfaceAgent().getName());
			msg.setContent(ToJSON.toJSon("id",this.agent.getCarData().getId()));

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