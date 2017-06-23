package behaviours;

import org.json.JSONObject;

import agents.CarAgent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CarReceivingDataBehaviour extends CyclicBehaviour {


	private static final long serialVersionUID = -8397872991964050209L;
	private MessageTemplate mtInform = 
			MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.INFORM),
					MessageTemplate.MatchOntology("roadStateOntology"));
	private CarAgent carAgent;
	
	public CarReceivingDataBehaviour(CarAgent carAgent) {
		super();
		this.carAgent = carAgent;
	}

	@Override
	public void action() {
		ACLMessage msg = myAgent.receive(mtInform);
		
		if (msg != null) {			
			// TODO: Integrate JSON data received from other cars in my 
			//       invision futureTraffic EDD for rerouting
			JSONObject datos = new JSONObject(msg.getContent());
			for(String key: datos.keySet()){
				carAgent.getFutureTraffic().
				         put(key, datos.getJSONObject(key));
			}
			
		} else block();

	}

}
