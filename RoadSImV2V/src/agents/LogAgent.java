package agents;

import behaviours.LogCarBehaviour;
import behaviours.LogSegmentBehaviour;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class LogAgent extends Agent {
	
	private static final long serialVersionUID = 1L;

	private String logginDirectory;
	
	protected void setup() {

		//Register the agent
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("logAgent");
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
		
		//Get the containers
		this.logginDirectory = (String) this.getArguments()[0];
		
		//Comportamientos de loggear
		addBehaviour(new LogCarBehaviour(this));
		addBehaviour(new LogSegmentBehaviour(this));
	}

}
