package behaviours;

import agents.LogAgent;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * Comportamiento que se encarga de loguear los datos de un coche
 * */

public class LogCarBehaviour extends Behaviour {
	
	private MessageTemplate mtLogCar = 
			MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.INFORM),
				MessageTemplate.MatchOntology("logCarOntology"));
	
	private LogAgent logAgent;
	private static final long serialVersionUID = 1L;

	public LogCarBehaviour() {
		// TODO Auto-generated constructor stub
	}

	public LogCarBehaviour(LogAgent a) {
		this.logAgent = a;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void action() {
		ACLMessage msg = logAgent.receive(mtLogCar);
		if (msg != null) { //There is a message
			//System.out.println("/***********/\n LogCarBehaviour:action \n" + msg);
			this.logAgent.writeCar(msg.getContent());
		}

	}

	@Override
	public boolean done() {
		// TODO Auto-generated method stub
		return false;
	}

}
