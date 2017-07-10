package behaviours;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import agents.CarAgent;
import agents.SegmentAgent;
import environment.Segment;
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
		// Ask to twin Segment of the current segment on other
		//     cars in my sensor space
		case 0:
			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
			msg.setOntology("roadTwinsOntology");

			//TODO: Revise how to obtain the IAD of the twin segments
			//      from Strings
			List<String> twins = carAgent.getCurrentSegment().
					                      getTwinSegments();
			for(int i = 0; i < twins.size(); i++) {
				msg.addReceiver(carAgent.getMap().
						        getSegmentByID(twins.get(i)).
						        getSegmentAgent().getAID());
			}

			JSONObject content = new JSONObject();
			content.put("x", carAgent.getX());
			content.put("y", carAgent.getY());
			content.put("radio", carAgent.getRatio());
			content.put("id", carAgent.getId());
			msg.setContent(content.toString());
			
			carAgent.send(msg);
			step++;
			break;
		// Recibe los ids de los agentes coche que hay en el segmento opuesto
		// Se les manda la informaci�n del coche en ese momento
		// Esta informaci�n se basa en el id, velocidad, pk e
		// informaci�n sobre el trafico que le interesa al vecino
		case 1: 
			//TODO: Revise how to manage all the responses from
			//      all the twin segments
			ACLMessage req = myAgent.receive(mtTwins);
			//System.out.println(req);
			if (req!= null){
				JSONObject contenido = new JSONObject(req.getContent());

				//Contenido tiene la siguiente estructura: 
				//   {"ids": ["234242@232", "ferf234123@",...]}
				JSONArray list = contenido.getJSONArray("ids");
				//System.out.println("CarAgent AID CSDB: " + carAgent.getAID() + " ListaID: " + list.toString());
				int numTwins = list.length();
				// Ojo con pedir la misma información varias veces 
				//     al mismo vehículo durante el tiempo en que  
				//     coinciden en el radio de localización => => 
				//     => => Lo hace el agente segmento
				if (numTwins == 0) {step++; return;} // NO hay vecinos
				// Si al menos hay un vecino con el que comunicarse ..
				ACLMessage msgInf = new ACLMessage(ACLMessage.INFORM);
				msgInf.setOntology("roadStateOntology");
				for(int i = 0; i < list.length(); i++) {
					msgInf.addReceiver(new AID(list.get(i).toString(),
							           true));
				}
				JSONObject json = new JSONObject();
				json.put("tini", carAgent.getTini());
				json.put("tfin", carAgent.getCurrentTick());
				json.put("speed", carAgent.getCurrentSpeed());
				json.put("position", carAgent.getCurrentPk());
				json.put("id", carAgent.getId());
				json.put("futureTraffic", 
						          carAgent.getPastTraffic().toJSON());
				
				msgInf.setContent(json.toString());
				// There are two options:
				// 1) Do a request/answer cycle and do not imclude a 
				//     content in the msg, and then this car receives
				//      just JGraph from each other car.
				// 2) Do not perform a request/answer cycle, just 
				//     inform other agents about my sensored data in 
				//     radio then, this car doesn't have to wait for
				//      a response.
				// Choosing option 2: 
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
		//System.out.println("Hecho");
		return step == 2;
	}

}
