package behaviours;

import org.json.JSONObject;

import agents.CarAgent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CarsAheadBehaviour extends CyclicBehaviour {

	private static final long serialVersionUID = 7963143335893634038L;
	
	MessageTemplate mt = MessageTemplate.
			                  MatchOntology("trafficCarsAheadOntology");
	
	private CarAgent myCarAgent;
	public CarsAheadBehaviour(CarAgent myCarAgent) {
		super();
		this.myCarAgent = myCarAgent;
	}
	@Override
	public void action() {
		ACLMessage msg = myAgent.receive(mt);
		if (msg!= null) {
			JSONObject densityData = new JSONObject(msg.getContent());
			if (densityData.get("key").equals("register")) 
				myCarAgent.getTravelData().
				           setCarsAhead(densityData.getInt("cars"));
			else myCarAgent.getTravelData().decreaseCarsAhead();
		} else block();
	}

}
