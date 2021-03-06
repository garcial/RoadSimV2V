package behaviours;

import agents.CarAgent;
import agents.SegmentAgent;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CarSendingDataBehaviour extends Behaviour {

	private static final long serialVersionUID = 8933540727048470789L;
	
	private MessageTemplate mtTwins = 
			MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.INFORM),
					MessageTemplate.MatchOntology("roadTwinsOntology"));
	
	int step;
	private CarAgent carAgent;
	
	public CarSendingDataBehaviour(CarAgent carAgent) {
		super();
		this.carAgent = carAgent;
		step = 0;
	}

	// two steps behaviour
	@Override
	public void action() {
		switch (step) {
		case 0:
			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
			msg.setOntology("roadTwinsOntology");
			// Ask to twin Segment of the current segment on other cars in my sensor space
			msg.addReceiver(new SegmentAgent().getAID()); // TODO:
			msg.setContent(carAgent.getX()+"#"+carAgent.getY()+"#"+carAgent.getRatio()+"#"+carAgent.getId());
			carAgent.send(msg);
			step++;
			break;
		case 1: 
			ACLMessage req = myAgent.receive(mtTwins);
			if (req!= null){
				String[] contenido = req.getContent().split("#");
				int numTwins = Integer.parseInt(contenido[contenido.length-1]);
				// Ojo con pedir la misma información varias veces al mismo
				//    vehículo durante el tiempo en que coinciden en el radio 
				//    de localización => Lo hace el agente segmento
				if (numTwins == 0) {step++; return;} // NO hay nada más que hacer
				// Si al menos hay un vecino con el que comunicarse ...
				ACLMessage msgInf = new ACLMessage(ACLMessage.INFORM);
				msgInf.setOntology("roadStateOntology");
				for(int i = 1; i < contenido.length; i++) {
					msgInf.addReceiver(new AID(contenido[i], true));
				}
				msgInf.setConversationId("Aquí va un JGraph");
				// There are two options:
				// 1) Do a request/answer cycle and do not imclude a content in the msg 
				//     and then each car receives just JGraph from each other car.
				// 2) Do not perform a request/answer cycle, just inform other agents in radio
				//     then, you dont have to wait for a response
				carAgent.send(msgInf);
				step++;
			} else block();
			break;
		default:
			break;
		}
	}

	@Override
	public boolean done() {
		return step == 2;
	}

}
