package behaviours;

import javax.swing.SwingUtilities;

import agents.InterfaceAgent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

/**
 * This behaviour is used by the InterfaceAgent and adds a new car to the GUI 
 * and executes a behaviour to update the speed of the car.
 *
 */
public class InterfaceAddCarBehaviour extends CyclicBehaviour {

	private static final long serialVersionUID = 1L;

	//Template to listen for the new communications from cars
	private MessageTemplate mtNewCar = 
			MessageTemplate.and(
					MessageTemplate.MatchPerformative(ACLMessage.INFORM),
					MessageTemplate.MatchOntology("newCarOntology"));
	
	private InterfaceAgent agent;
	
	public InterfaceAddCarBehaviour(InterfaceAgent a) {
		
		this.agent = a;
	}

	@Override
	public void action() {

		ACLMessage msg = myAgent.receive(mtNewCar);
		
		if (msg != null) {
			
			//Get the data
			String cont = msg.getContent();

			final String id = cont.substring(cont.indexOf("id=")+3, cont.indexOf("algorithmType="));
			final float x = Float.parseFloat(cont.substring(
					cont.indexOf("x=")+2, cont.indexOf("y=")));
			final float y = Float.parseFloat(cont.substring(
					cont.indexOf("y=")+2, cont.indexOf("id=")));
			final int algorithmType = Integer.parseInt(cont.substring(
					cont.indexOf("algorithmType=")+14));
			

			//Add the car to the scene
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					agent.getMap().addCar(myAgent.getLocalName(), id, algorithmType, x, y, false);
				}
			});
			
		} else block();
	}
}