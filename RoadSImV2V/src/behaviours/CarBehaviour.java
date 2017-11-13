package behaviours;
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

	private CarAgent carAgent;
	private AID topic;
	private boolean done = false;
	private boolean drawGUI;
	private long previousTick;
	private long currentTick;
	private float stepDistanceCovered;


	public CarBehaviour(CarAgent a, long timeout, boolean drawGUI) {

		this.carAgent = a;
		this.drawGUI = drawGUI;
		this.topic = null;
		previousTick = carAgent.getSimulationData().getInitialTick() - 1;
		this.stepDistanceCovered = 0f;
		
		try {
			TopicManagementHelper topicHelper =(TopicManagementHelper) 
				this.carAgent.getHelper(TopicManagementHelper.
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
			this.carAgent.getSimulationData().setCurrentTick(currentTick);

			//If I still have to move somewhere
			if(this.carAgent.getPath().getGraphicalPath().size() > 0) {
				//Get the path
				Step currentStep = this.carAgent.getPath().
								   getGraphicalPath().get(0);
				
				// First calculate the currentSpeed by Greenshield model
				// currentTrafficDensity of the being ahead my current position
				double currentTrafficDensity = 
						carAgent.getTravelData().getCarsAhead() /
						(carAgent.getTravelData().getCurrentSegment().getLength() -
						 carAgent.getTravelData().getSegmentDistanceCovered());
						
				int currentSpeed = (int) Math.min(
						carAgent.getCarData().getMaxSpeed(),
					    (currentTrafficDensity >= 43f)? 
					     5:
					     carAgent.getTravelData().getCurrentSegment().getMaxSpeed()
					        * (1-currentTrafficDensity/43f));

				carAgent.getCarData().setCurrentSpeed(currentSpeed);

				// Update pkCurrent with this speed and the difference
				//   between previousTick and currentTick
				//   We transform km/h to k/s if divide it by 3600
				
				float currentPk = this.carAgent.getTravelData().getCurrentPK();
				float deltaTime = (currentTick - previousTick) / 3600f;
				float deltaPk = (float) currentSpeed * deltaTime;
				
				float graphCovered = deltaPk * 
				        currentStep.getStepGraphicalLength() /
				        currentStep.getStepLength();
		
				//Virtual position
				float currentX = this.carAgent.getCarData().getX();
				float currentY = this.carAgent.getCarData().getY();
				
				//Update step distance covered
				stepDistanceCovered += deltaPk;
				carAgent.getTravelData().increaseSegmentDistanceCovered(deltaPk);
				carAgent.getTravelData().increaseTripDistanceCovered(deltaPk);

				//Check if we need to go to the next step

				while (stepDistanceCovered > currentStep.getStepLength()) {
					stepDistanceCovered -= currentStep.getStepLength();
					//If there is still a node to go
					if (this.carAgent.getPath().getGraphicalPath().size()> 1) {
						//Remove the already run path
						this.carAgent.getPath().getGraphicalPath().remove(0);
						currentStep = this.carAgent.getPath().
								           getGraphicalPath().get(0);
						currentX = currentStep.getOriginX();
						currentY = currentStep.getOriginY();				
					} else {
						this.kill();
						break;
					}
				}

				if (!this.done) {
										
					//Update the current pk when update the x and y
					if("up".compareTo(carAgent.getTravelData().
							          getCurrentSegment().getDirection())
							== 0) {
						carAgent.getTravelData().
						         setCurrentPK(currentPk + stepDistanceCovered);
					} else {
						carAgent.getTravelData().
						         setCurrentPK(currentPk - stepDistanceCovered);
					}
					
					// Compute the next graphical position
					float proportion = graphCovered / 
							           currentStep.getStepGraphicalLength();

					this.carAgent.getCarData().setX(
							currentX + proportion * (currentStep.getDestinationX()
									                 - currentStep.getOriginX()));
					this.carAgent.getCarData().setY(currentY + proportion * 
							(currentStep.getDestinationY() - 
							 currentStep.getOriginY()));

					//If I am in a new segment
					if (!carAgent.getTravelData().getCurrentSegment().
							equals(currentStep.getSegment())) {
						long tfin = Long.parseLong(msg.getContent());

						//delete the surplus of km added to the previous segment 
						this.carAgent.getTravelData().
						     increaseSegmentDistanceCovered(-stepDistanceCovered);

						//Deregister from previous segment
						Segment previousSegment = carAgent.getTravelData().getCurrentSegment();
						this.informSegment(previousSegment,"deregister");
						
						//Set the new previous segment
						this.carAgent.getTravelData().
						     setCurrentSegment(currentStep.getSegment());
						
						this.carAgent.getTravelData().
						     setSegmentDistanceCovered(stepDistanceCovered);

						//Register in the new segment
						this.informSegment(currentStep.getSegment(),"register");
						
						carAgent.getSimulationData().setInitialTick(tfin);
						
						// TODO:If we are using the smart algorithm, 
						//  recalculate all the traffic states on the 
						//  map with the information provided from 
						//  othercarAgents, and then rerouting 
						//  accordingly.
						// TODO: futureTrafficStore analysis

						carAgent.getTravelData().
						         setCurrentPK(currentStep.getSegment().getPkIni());
						if("up".compareTo(carAgent.getTravelData().
								          getCurrentSegment().getDirection()) 
								== 0) {
							carAgent.getTravelData().
							         setCurrentPK(currentPk + stepDistanceCovered);
						} else {
							carAgent.getTravelData().
							         setCurrentPK(currentPk - stepDistanceCovered);
						}
						
						if (this.carAgent.isSmart()) {
								this.carAgent.recalculate(previousSegment.getId(),
										                 carAgent.getTravelData().
										                      getCurrentSegment().
										                     getOrigin().getId());
						}
						
						// Once rerouted, Delete data from future 
						//     Traffic related to this new segment
						carAgent.getFutureTraffic().
						         delete(currentStep.getSegment().getId());
						//agent.getPastTraffic().
						//    put(previousSegmentId,agent.getSensorTrafficData());
					}

					this.informSegment(currentStep.getSegment(), "update");
					previousTick = currentTick;
					carAgent.addBehaviour(
							    new CarSendingDataBehaviour(carAgent));
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
		
		//TODO Ahora se va a desregistrar del segmento. En este momento hay que
		//     reconfigurar los
		//TrafficData y poner el grafo como toca.
		if ("deregister".compareTo(type) == 0) {
			// Introducir el Tfin en TrafficData
			this.carAgent.getSensorTrafficData().setTfin(this.currentTick);
			// First give the number of cars detected
			this.carAgent.getSensorTrafficData().
			        setNumCars(carAgent.getSensorTrafficData().
			        	    	getCarsPositions().size());
			
			//System.out.println("CB Pasamos de sensor a past en " + 
			//      segment.getId() + " de " + this.agent.getName() + " - " + 
			//      this.agent.getSensorTrafficData() );
			this.carAgent.getPastTraffic().
			     put(segment.getId(), this.carAgent.getSensorTrafficData());
			
			//Cambiar el graph
			//System.out.println("Cambiar el grafo futuro en "+segment.getId());
			for(String seg: this.carAgent.getFutureTraffic().getData().keySet()){
				Edge edge =  this.carAgent.getGraph().getEdgeById(seg);
				long tiniAux = edge.getTini();
				long tfinAux = edge.getTfin();
				TrafficData dataAux = null;
				for(TrafficData t : 
					this.carAgent.getFutureTraffic().getData().get(seg)) {
					// TODO Voy a utilizar que el tini haya empezado antes  
					//   y en el caso de que sean igual el que tfin sea más 
					//   grande pasa
					if((t.getTfin() > tfinAux) || 
					   (t.getTfin() == tfinAux && t.getTini() < tiniAux) ) {
						tiniAux = t.getTini();
						tfinAux = t.getTfin();
						dataAux = t;
					}
				}
				
				if(dataAux != null){
					int serviceLevel = calculateServiceLevel(
							            dataAux.getNumCars(), 
							            carAgent.getTravelData().
							                     getCurrentSegment().getLength());
					// TODO: Aqui el nivel de servicio no se cual poner. 
					//       Que calculo con el número de coches he de hacer
					
					edge.updateEdge(seg,serviceLevel, 
						  carAgent.getTravelData().getCurrentSegment().
						           getLength() / carAgent.getTravelData().
							       getCurrentSegment().getCurrentAllowedSpeed() ,
							       dataAux.getTini(), dataAux.getTfin());
				}
				
			}
			//System.out.println(this.agent.getJgrapht().getEdges());
			/*System.out.println("PAST TRAFFIC de " + this.agent.getId());
			for (String key : this.agent.getPastTraffic().getData().keySet()) {
				System.out.println("CBinfSeg: " + key + " - " +
				             this.agent.getPastTraffic().getData().get(key));
			}
			System.out.println("Distancia: " + (float)segment.getLength()*1000);
			System.out.println("Distancia real: " + 
			                   (float) this.currentSegmentCovered);
			System.out.println("Tiempo: " + this.agent.getSensorTrafficData().
			                    getTfin() + " - " + this.agent.
			                    getSensorTrafficData().getTini() );*/
			//TODO: Revise this formula is 3600 multiplying up or down 
			//      the division?
			float vel = ( carAgent.getTravelData().getSegmentDistanceCovered() / 
					(carAgent.getSensorTrafficData().getTfin() - 
					 this.carAgent.getSensorTrafficData().getTini()) * 3600);
			//this.carAgent.addLogData(segment.getId(), this.carAgent.
			//     getNumMsgRecibido(), this.carAgent.getNumMsgEnviados(), 
			//     (float) this.currentSegmentCovered, vel);
			//We reinicializate the number og messages
			//this.carAgent.setNumMsgEnviados(0);
			//this.carAgent.setNumMsgRecibido(0);
			
			//Update the EDGE
			/*Es decir tenemos que recorrer toda la lista buscando
			 * el que tenga la diferencia de tiempos mayor y a partir de 
			 * ahí generar un edge y guardarlo en el jgrapht.*/
			//Calculate the better way
		}else if("register".compareTo(type) == 0){
			//Start a new current trafficData by myself
			carAgent.getTravelData().setSegmentDistanceCovered(0);
			//agent.setFutureTraffic(new TrafficDataInStore());
			carAgent.setSensorTrafficData(new TrafficData());
			carAgent.getSensorTrafficData().setTini(this.currentTick);
			//To log the info segment we need the initial tick
		} else if("update".compareTo(type) == 0){
			
		}
		
		msg.addReceiver(segment.getSegmentAgent().getAID());
		JSONObject carDataRegister = new JSONObject();
		carDataRegister.put("id", this.carAgent.getCarData().getId());
		carDataRegister.put("x", this.carAgent.getCarData().getX());
		carDataRegister.put("y", this.carAgent.getCarData().getY());
		carDataRegister.put("speed", 
				            this.carAgent.getCarData().getCurrentSpeed());
		carDataRegister.put("type", 
				            this.carAgent.getCarData().getTypeOfAlgorithm());
		carDataRegister.put("segmentDistanceCovered", this.stepDistanceCovered);
		carDataRegister.put("tick", this.currentTick);
		carDataRegister.put("radio", this.carAgent.getCarData().getRadio());
		carDataRegister.put("initialTick", 
				            this.carAgent.getSimulationData().getInitialTick());
		carDataRegister.put("tripDistanceCovered", 
				        this.carAgent.getTravelData().getTripDistanceCovered());
		
		msg.setContent(carDataRegister.toString());
		myAgent.send(msg);
	}

	public void kill() {

		//Done flag
		this.done = true;
		//Deregister from previous segment
		this.informSegment(this.carAgent.getTravelData().getCurrentSegment(),
				           "deregister");
		ACLMessage msgLog = new ACLMessage(ACLMessage.INFORM);
		msgLog.setOntology("logCarOntology");
		msgLog.addReceiver(this.carAgent.getLogAgent().getName());
		myAgent.send(msgLog);

		//Delete the car from the canvas
		if (this.carAgent.getInterfaceAgent() != null && this.drawGUI) {

			ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
			msg.setOntology("deleteCarOntology");
			msg.addReceiver(this.carAgent.getInterfaceAgent().getName());
			msg.setContent(ToJSON.toJSon("id",
					                     this.carAgent.getCarData().getId()));

			myAgent.send(msg);
		}
		
		//Deregister the agent
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(this.carAgent.getAID());
		
		try {
			DFService.deregister(this.carAgent,  dfd);
		} catch (Exception e) { 
		}

		this.carAgent.doDelete();
	}
}