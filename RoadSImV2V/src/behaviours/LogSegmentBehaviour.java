package behaviours;

import agents.LogAgent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * Comportamiento que se encarga de loguear los datos de un segmento
 * */

public class LogSegmentBehaviour extends Behaviour {

	
	private MessageTemplate mtLogSegment = 
			MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.INFORM),
				MessageTemplate.MatchOntology("logSegmentOntology"));
	
	private static final long serialVersionUID = 1L;
	
	private LogAgent logAgent;
	
	public LogSegmentBehaviour() {
		// TODO Auto-generated constructor stub
	}

	public LogSegmentBehaviour(LogAgent a) {
		this.logAgent = a;
	}

	@Override
	public void action() {
		ACLMessage msg = logAgent.receive(mtLogSegment);
				
		if (msg != null) { //There is a message
			/*Here we have de content to send to the agent*/
			this.logAgent.writeSegment(msg.getContent());
		}
		
	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}
}
