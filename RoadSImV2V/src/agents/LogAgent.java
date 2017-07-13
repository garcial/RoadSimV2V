package agents;

import java.io.FileWriter;
import java.io.PrintWriter;

import org.json.JSONObject;

import behaviours.LogCarBehaviour;
import behaviours.LogSegmentBehaviour;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class LogAgent extends Agent {
	
	private static final long serialVersionUID = 1L;

	private String logginDirectory;
	
	protected void setup() {

		//Register the agent
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("logAgent");
		sd.setName(getLocalName());

		dfd.addServices(sd);
		try {
			DFService.register(this,  dfd);
		} catch (FIPAException fe) {
			
			//Sometimes an agent cannot find the DF in time
			//I still don't know when this happens so I will
			//simply kill it for now.
			this.takeDown();
		}
		
		//Get the containers
		this.logginDirectory = (String) this.getArguments()[0];
		
		//Comportamientos de loggear
		addBehaviour(new LogCarBehaviour(this));
		addBehaviour(new LogSegmentBehaviour(this));
	}
	
	public void writeCar(JSONObject json){
		 FileWriter fichero = null;
	        PrintWriter pw = null;
	        try
	        {
	            fichero = new FileWriter(logginDirectory);
	            pw = new PrintWriter(fichero);

	            for (int i = 0; i < 10; i++)
	                pw.println("Linea " + i);

	        } catch (Exception e) {
	            e.printStackTrace();
	        } finally {
	           try {
	           // Nuevamente aprovechamos el finally para 
	           // asegurarnos que se cierra el fichero.
	           if (null != fichero)
	              fichero.close();
	           } catch (Exception e2) {
	              e2.printStackTrace();
	           }
	        }
	}
	
	public void writeSegment(JSONObject json){
		FileWriter fichero = null;
        PrintWriter pw = null;
        try
        {
            fichero = new FileWriter(logginDirectory);
            pw = new PrintWriter(fichero);

            for (int i = 0; i < 10; i++)
                pw.println("Linea " + i);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
           try {
           // Nuevamente aprovechamos el finally para 
           // asegurarnos que se cierra el fichero.
           if (null != fichero)
              fichero.close();
           } catch (Exception e2) {
              e2.printStackTrace();
           }
        }
	}
}
