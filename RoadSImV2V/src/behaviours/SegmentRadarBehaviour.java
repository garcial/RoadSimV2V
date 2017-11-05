package behaviours;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import agents.SegmentAgent;
import features.CarData;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * Gestiona la comunicación entre agentes coches
 * */

public class SegmentRadarBehaviour extends CyclicBehaviour {

	private static final long serialVersionUID =-3953532297570848414L;

	//Template to listen for the new communications from cars
	private MessageTemplate mtCarRadar = 
			MessageTemplate.and(
				MessageTemplate.MatchPerformative(ACLMessage.REQUEST),
				MessageTemplate.MatchOntology("roadTwinsOntology"));
	
	private SegmentAgent mySegmentAgent;
	
	public SegmentRadarBehaviour(SegmentAgent mySegmentAgent){
		this.mySegmentAgent = mySegmentAgent;
	}
	@Override
	public void action() {

		ACLMessage msg = mySegmentAgent.receive(mtCarRadar);
		
		if (msg != null) { //There is a message
			JSONObject obj = new JSONObject(msg.getContent());
			String idSolicitante = obj.getString("id");
			int x = obj.getInt("x");
			int y = obj.getInt("y");
			int radio = obj.getInt("radio");

			List<String> twins = new ArrayList<String>();
			
			HashMap<String, CarData> cars = mySegmentAgent.getCars();
			//System.out.println("CARS: " +cars.toString());
			for(String id :cars.keySet()) {
				//System.out.println("ID SEGMENT RADAR: " + id);
				CarData cd = cars.get(id);
				//System.out.println("CD: " + cd.toString());
				double dist = Math.sqrt((x-cd.getX())*(x-cd.getX()) + 
						(y-cd.getY())*(y-cd.getY()));
				if (dist <= radio && dist <= cd.getRadio()){
					twins.add(id);
				}
			}
			twins.remove(idSolicitante);
			//System.out.println(twins);
			//System.out.println("TWINS: " + twins.toString());
			// Build msg to answer the carAgent requesting
			ACLMessage msgCarsOnRadio = 
					               new ACLMessage(ACLMessage.INFORM);
			// Filter just cars not used before for this carAgent

			JSONObject objres = new JSONObject();
			JSONArray list = new JSONArray();
			for(String id:twins) {
				if (mySegmentAgent.isNewCommunication(idSolicitante, id)) {
					list.put(id);
				}
			}
			objres.put("ids", list);
			msgCarsOnRadio.setOntology("roadTwinsOntology");

			msgCarsOnRadio.setContent(objres.toString());
			msgCarsOnRadio.addReceiver(msg.getSender());
			mySegmentAgent.send(msgCarsOnRadio);

		} else block();
	}

}
