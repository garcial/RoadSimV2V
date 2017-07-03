package behaviours;

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
		case 0:
			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
			msg.setOntology("roadTwinsOntology");
			// Ask to twin Segment of the current segment on other cars 
			//    in my sensor space
			//Tengo el string id de segmento ahora como consigo el segmento y su agente
			//Tengo dos ideas o consigo el segmento a partir de la intersecci�n o a partir de los edges del JGrapht
			for(String sa : this.carAgent.getCurrentSegment().getTwinSegments()){
				//System.out.println(sa);
				//Solo necesito mirar los que entran porque si yo he salido mi gemelo no puede salir de la
				// intersecci�n ha de entrar
				for(Segment s: this.carAgent.getCurrentSegment().getOrigin().getInSegments()){
					if(sa.compareTo(s.getId()) == 0){
						msg.addReceiver(s.getSegmentAgent().getAID());
					}
				}
			}
			

			JSONObject content = new JSONObject();
			content.put("x", carAgent.getX());
			content.put("y", carAgent.getY());
			content.put("radio", carAgent.getRatio());
			content.put("id", carAgent.getId());
			//System.out.println("Se envia informaci�n: " + carAgent.getX() + " " + carAgent.getY() + " " + carAgent.getId());
			msg.setContent(content.toString());
			
			carAgent.send(msg);
			step++;
			break;
		case 1: 
			ACLMessage req = myAgent.receive(mtTwins);
			//System.out.println(req);
			if (req!= null){
				System.out.println("CASO 1 de Car Sending");
				JSONObject contenido = new JSONObject(req.getContent());
				//Contenido tiene la siguiente estructura: 
				//   {"ids": ["234242@232", "ferf234123@",...]}
				JSONArray list = contenido.getJSONArray("ids");
				int numTwins = list.length();
				// Ojo con pedir la misma información varias veces al 
				//    mismo vehículo durante el tiempo en que coinciden 
				//     en el radio de localización => => =>
				//     => => Lo hace el agente segmento
				if (numTwins == 0) {step++; return;} // NO hay vecinos
				// Si al menos hay un vecino con el que comunicarse ...
				ACLMessage msgInf = new ACLMessage(ACLMessage.INFORM);
				msgInf.setOntology("roadStateOntology");
				for(int i = 1; i < list.length(); i++) {
					msgInf.addReceiver(new AID(list.get(i).toString(),
							           true));
				}
				JSONObject json = new JSONObject();
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
				//     inform other agents about my sensored data in radio
				//     then, this car doesn't have to wait for a response.
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
