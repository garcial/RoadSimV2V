package behaviours;

import org.json.JSONObject;

import agents.CarAgent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class CarReceivingDataBehaviour extends CyclicBehaviour {

	private static final long serialVersionUID =-8397872991964050209L;
	private MessageTemplate mtInform = 
			MessageTemplate.and(
					MessageTemplate.MatchPerformative(
							ACLMessage.INFORM),
					MessageTemplate.MatchOntology(
							"roadStateOntology"));
	private CarAgent carAgent;
	
	public CarReceivingDataBehaviour(CarAgent carAgent) {
		super();
		this.carAgent = carAgent;
	}

	@Override
	public void action() {
		ACLMessage msg = myAgent.receive(mtInform);
		
		if (msg != null) {		
			//System.out.println("RECIBIENDO informaci�n");
			// Integrate JSON data received from other cars in my 
			//       invision futureTraffic EDD for rerouting
			JSONObject datos = new JSONObject(msg.getContent());
			// Received current id, current speed,
			//   current position and pastTrafficState (that is to 
			//   be added to futureTrafficState of the agent that
			//   receives this msg)
			// TODO: Adem�s de introducir los datos tambien deber�a contar el coche no?
			carAgent.getSensorTrafficData().getCarsSpeeds().
			                         add(datos.getDouble("speed"));
			carAgent.getSensorTrafficData().getCarsPositions().
			                         add(datos.getDouble("position"));
			carAgent.getSensorTrafficData().getIds().add(datos.getString("id"));
			
			for(String key:datos.getJSONObject("futureTraffic").
					             keySet()){
				//System.out.println(datos.getJSONObject("futureTraffic").get(key));
				carAgent.getFutureTraffic().
		         put(key, (JSONObject) datos.getJSONObject("futureTraffic").get(key));
			}		
			
			for(String key:carAgent.getFutureTraffic().getData().keySet()){
				System.out.println("CRDB Key: "+ key + " Data: " + carAgent.getFutureTraffic().getData().get(key));
			}
		} else block();

	}

}
