package agentsNoInterface;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.messaging.TopicManagementHelper;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * This agent is in charge of sending a Tick message to the rest of the
 * agents.
 * 
 * EvenManagerAgent, CarAgents and SegmentAgents need a tick to start
 * its behaviour, otherwise they won't do anything.
 *
 */
public class TimeKeeperAgent extends Agent {

	private static final long serialVersionUID = 4546329963020795810L;
	private long tickLength, currentTick, finishingTick;
	private List<AID> agents = new ArrayList<AID>();
	private DFAgentDescription interfaceAgent;

	protected void setup() {

		//Register the service
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("timeKeeperAgent");
		sd.setName(getLocalName());

		dfd.addServices(sd);
		try {
			DFService.register(this,  dfd);
		} catch (FIPAException fe) { 
			fe.printStackTrace(); 
		}

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

		this.interfaceAgent = result[0];

		//Get the ticklength
		this.tickLength = (long) this.getArguments()[0];

		//Start at specific tick
		this.currentTick = (long) this.getArguments()[1];

		//End the simulation at specific tick
		this.finishingTick = (long) this.getArguments()[2];

		//A reference to myself
		TimeKeeperAgent timeKeeperAgent = this;

		//Create the tick topic
		AID topic = null;
		try {
			TopicManagementHelper topicHelper = (TopicManagementHelper) getHelper(TopicManagementHelper.SERVICE_NAME);
			topic = topicHelper.createTopic("tick");

		} catch (Exception e) {
			System.err.println("Agent " + getLocalName() + ": ERROR creating topic \"tick\"");
			e.printStackTrace();
		}

		final AID finalTopic = topic;

		addBehaviour(new Behaviour() {

			private static final long serialVersionUID = 1L;

			@Override
			public void action() {

				if (timeKeeperAgent.currentTick == timeKeeperAgent.finishingTick) {

					System.exit(0);
				}

				//I was using a TickerBehaviour, but you cannot change the tick length
				try {
					Thread.sleep(((TimeKeeperAgent) myAgent).getTickLength());
				} catch (InterruptedException e) {
					System.out.println("Bye");
				}

				if (timeKeeperAgent.getTickLength() > 0){
					try {
						Thread.sleep(timeKeeperAgent.getTickLength());
					} catch (InterruptedException e1) {

						e1.printStackTrace();
					}
				}

				timeKeeperAgent.currentTick++;

				//Aquí hay una comunicación que no sé donde va
				ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
				msg.addUserDefinedParameter(ACLMessage.IGNORE_FAILURE, "true");
				msg.addUserDefinedParameter(ACLMessage.DONT_NOTIFY_FAILURE, "true");
				msg.addUserDefinedParameter(ACLMessage.SF_TIMEOUT, "-1");
				msg.addReceiver(finalTopic);
				msg.setContent(Long.toString(timeKeeperAgent.currentTick));
				myAgent.send(msg);
				
				//Send the number of cars to the interface agent
				//Search for cars that are currently in the DF
				DFAgentDescription[] cars = null;

				DFAgentDescription dfd = new DFAgentDescription();
				ServiceDescription sd  = new ServiceDescription();
				sd.setType("carAgent");
				dfd.addServices(sd);

				try {
					cars = DFService.search(
							timeKeeperAgent, getDefaultDF(), dfd, null);
				} catch (FIPAException e) { 

					e.printStackTrace(); 
				}

				if (cars != null) {
					msg = new ACLMessage(ACLMessage.INFORM);
					msg.addReceiver(interfaceAgent.getName());
					msg.setOntology("numberOfCarsOntology");
					//msg.setContent(Integer.toString(cars.length));
					JSONObject numberOfCars = new JSONObject();
					numberOfCars.put("numberOfCars", cars.length);
					
					msg.setContent(numberOfCars.toString());
					myAgent.send(msg);
				}
			}

			@Override
			public boolean done() {
				return false;
			}
		});

		/**
		 * End of test code
		 */

		//Check for tickLeght changes
		addBehaviour(new Behaviour() {

			private static final long serialVersionUID = 8455875589611369392L;

			MessageTemplate mt = MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.INFORM), 
					MessageTemplate.MatchOntology("changeTickLengthOntology"));

			@Override
			public void action() {

				ACLMessage msg = myAgent.receive(mt);

				if (msg != null) {
					// ((TimeKeeperAgent)this.myAgent).setTickLength(Long.parseLong(msg.getContent()));
					JSONObject messageData = new JSONObject(msg.getContent()); 
					Long tickLength = (long) (messageData.getInt("idTick"));
					System.out.println(tickLength);
					((TimeKeeperAgent)this.myAgent).setTickLength(tickLength);
				} else block();
			}

			@Override
			public boolean done() {
				return false;
			}
		});
	}

	//Setters and getters
	public List<AID> getAgents() {
		return agents;
	}

	public long getTickLength() {

		return this.tickLength;
	}

	public void setTickLength(long newTick) {

		this.tickLength = newTick;
	}
}