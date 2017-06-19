package behavioursNoInterface;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CarReceivingDataBehaviour extends CyclicBehaviour {


	private static final long serialVersionUID = -8397872991964050209L;
	private MessageTemplate mtInform = 
			MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.INFORM),
					MessageTemplate.MatchOntology("roadStateOntology"));
	
	@Override
	public void action() {
		ACLMessage msg = myAgent.receive(mtInform);
		
		if (msg != null) {
			
			// TODO: Integrate JGraph received from other cars in my 
			//       invision JGraph to rerouting
			String jgraph = msg.getContent();
		} else block();

	}

}
