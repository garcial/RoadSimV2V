package environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Class that holds the representation of a traffic map.
 * 
 * It also has al the logic to read the traffic map files and creates the 
 * {@link SegmentAgent}.
 *
 */
public class TrafficMap implements Serializable {

	private static final long serialVersionUID = 6521810168990354805L;

	private Integer intersectionCount;
	private Integer segmentCount;

	//The container where the segment agents will be created
	private transient jade.wrapper.AgentContainer mainContainer;
	
	private boolean useLog;
	private String loggingDirectory;
	Map<String, Intersection> intersectionsMap;
	Map<String, Segment> segmentsMap;
	// A list with virtualNodes to compute the shortest path is need here.
	List<String> virtualNodes;
	private boolean drawGUI;

	/**
	 * Constructor that builds a Map from a folder.
	 * 
	 * @param folder Folder where the files are stored.
	 */
	public TrafficMap(String folder, 
			   jade.wrapper.AgentContainer mainContainer,
			   boolean useLog, String loggingDirectory, 
			   boolean drawGUI, long tick) 
		   throws IOException {

		//For the agents
		this.mainContainer = mainContainer;		
		this.useLog = useLog;
		this.loggingDirectory = loggingDirectory;

		//Read the files
		this.intersectionCount = 0;
		this.segmentCount = 0;
		
		this.drawGUI = drawGUI;

		//Get all files from the given folder
		String url = TrafficMap.class.getClassLoader().getResource(folder).
				                                       getPath();
		
		File[] files = new File(url).listFiles();

		BufferedReader intersectionsReader = null, 
				       segmentsReader = null, 
				       stepsReader = null;

		for(int i=0; i < files.length; i++) {
			
			if(files[i].getName().equals("intersections")){

				intersectionsReader = new 
						BufferedReader(
						new FileReader(files[i].getAbsolutePath()));

			}else if(files[i].getName().equals("segments")){

				segmentsReader = new BufferedReader(
						new FileReader(files[i].getAbsolutePath()));

			}else if(files[i].getName().equals("steps")){

				stepsReader = new BufferedReader(
						new FileReader(files[i].getAbsolutePath()));
			}
		}

		if(segmentsReader == null || intersectionsReader == null || 
				stepsReader == null) {

			throw new IOException("Couldn't find the files.");
		} else {
			try {
				intersectionsMap = new HashMap<String, Intersection>();

				String line = intersectionsReader.readLine();
				while(line != null){

					JSONObject inter = new JSONObject(line);

					Intersection intersection = new 
							Intersection(inter.getString("id"), 
							    inter.getJSONObject("coordinates").
							          getInt("x"),
								inter.getJSONObject("coordinates").
								      getInt("y"));

					intersectionsMap.put(inter.getString("id"),
							             intersection);
					
					line = intersectionsReader.readLine();
					this.intersectionCount++;
				}

				virtualNodes = new ArrayList<String>();
				segmentsMap = new HashMap<String, Segment>();

				line = segmentsReader.readLine();
				while(line != null){

					JSONObject seg = new JSONObject(line);

					Intersection origin = null;
					Intersection destination = null;

					//Origin
					if(!seg.getString("origin").equals("null")) {

						origin = intersectionsMap.get(seg.getString("origin"));
					}

					//Destination
					if(!seg.getString("destination").equals("null")) {

						destination = intersectionsMap.get(
								                  seg.getString("destination"));
					}

					// Retrieve the twin segment from the current one
					JSONArray segTwinsJSON = seg.getJSONArray("twins");
					LinkedList<String> segTwinsList = new LinkedList<String>();
					for (int i = 0; i < segTwinsJSON.length(); i++){
						segTwinsList.add((String)segTwinsJSON.get(i));
					}
					
					//Make the segment
					Segment segment = new Segment(seg.getString("id"), 
									          origin, destination, 
									          seg.getDouble("length"),
									          seg.getInt("maxSpeed"), 
									          seg.getInt("capacity"),
									          seg.getInt("density"), 
									          seg.getInt("numberTracks"), 
									          this.mainContainer, this.useLog, 
									          this.loggingDirectory, this.drawGUI,
									          seg.getString("direction"),
									          seg.getDouble("pkstart"), 
									          segTwinsList,
									          seg.getString("roadCode"),tick, 0);

					if(origin != null){
						origin.addOutSegment(segment);
						virtualNodes.add(segment.getId() + "#" + 
						                 segment.getDestination().getId());
					}

					if(destination != null){
						destination.addInSegment(segment);
					}
															
					//Add an Edge to the Multigraph
//					if(origin != null && destination != null){	
//						this.graph.addEdge(edgeSegment);
//						/* The weight is hours in double (0.xx) */
//						//TODO: Explain this, please!!!
//						// This modification of the weight of the edge is because
//						//    the service level is 2 when we haven't
//						// any communication about the segment from other agent
//						edgeSegment.setWeight(seg.getDouble("length") / 
//								             (seg.getInt("maxSpeed") * 0.8f));
//					}

					segmentsMap.put(segment.getId(), segment);

					line = segmentsReader.readLine();
					this.segmentCount++;
				}

				//TODO: PUT ALLOWED WAYS. IN THIS CASE ALL THE WAYS ARE ALLOWED
				//Esto es como si todas las intersecciones fueran rotondas
				for(Intersection intersection : intersectionsMap.values()){
					for(Segment in: intersection.getInSegments()){
						for(Segment out: intersection.getOutSegments()){
							intersection.addAllowedWay(in.getId(), out.getId());
						}
					}
				}
				
				//Read all the stepsTest
				line = stepsReader.readLine();

				//Read all the segmentsTest
				while(line != null){

					JSONObject step = new JSONObject(line);

					//The segment the step belongs to
					String idSegment = step.getString("idSegment");

					//Create the step
					Step s = new Step(step.getString("id"), 
						segmentsMap.get(idSegment), 
						step.getJSONObject("originCoordinates").getInt("x"),
						step.getJSONObject("originCoordinates").getInt("y"),
						step.getJSONObject("destinationCoordinates").getInt("x"),
						step.getJSONObject("destinationCoordinates").getInt("y"));

					//Add the stepsTest to the segment
					segmentsMap.get(idSegment).addStep(s);				

					line = stepsReader.readLine();
				}
				
				//Move the segmentsTest
				for (String string : segmentsMap.keySet()) {
					this.move(segmentsMap.get(string), 4);
				}
				
				//Compute the length of the step according to the
				//   length of the segment
				for(Segment segment:segmentsMap.values()) {
					double length = 0.0;
					for(Step step:segment.getSteps()) {
						length += step.getStepGraphicalLength(); 
					}
					for(Step step:segment.getSteps()) {
						step.setStepLength((float) 
								(step.getStepGraphicalLength() *
								 segment.getLength() / length));
					}
				}

			}catch(Exception e){
				e.printStackTrace();
			}finally{
				intersectionsReader.close();
				segmentsReader.close();
				stepsReader.close();
			}
		}
	}

	/**
	 * Given the id of an intersection, it returns that intersection
	 * 
	 * @param id
	 * @return
	 */
	public Intersection getIntersectionByID(String id){
		return intersectionsMap.get(id);
	}

	/**
	 * Returns the list of intersectionsTest
	 * 
	 * @return The Intersection list
	 */
	public Collection<Intersection> getIntersections(){

		return this.intersectionsMap.values();
	}

	/**
	 * This method moves the segment a given quantity, so two  
	 * segmentsTest don't overlap.
	 * 
	 * @param seg
	 * @param quantity
	 */
	private void move(Segment seg, int quantity) {

		List<Step> steps = seg.getSteps();

		Step firstStep = steps.get(0);
		Step lastSetp = steps.get(steps.size()-1);

		//We will use this to check if the segment is more 
		//   horizontal than vertical
		int xIncrement = 
			lastSetp.getOriginX() - firstStep.getDestinationX();
		int yIncrement = 
			lastSetp.getOriginY() - firstStep.getDestinationY();

		if (xIncrement > yIncrement) { //The line is more horizontal

			if (firstStep.getOriginX() < lastSetp.getDestinationX()) { 
				//Left to right
				this.moveY(steps, quantity); //Move down
			} else {

				this.moveY(steps, -quantity); //Move up
			}
			
		} else { //The line is more vertical

			if (firstStep.getOriginY() > lastSetp.getDestinationY()) { 
				//Bottom to up
				this.moveX(steps, quantity); //Move right
			} else {

				this.moveX(steps, -quantity); //Move left
			}
		}
	}

	private void moveX(List<Step> stepList, int quantity){
		
		for (int i=0; i < stepList.size(); i++) {
			
			Step step = stepList.get(i);
			
			if (i == 0) { //First, we don't move its origin	
				
				step.setDestinationX(step.getDestinationX()+quantity);
				
			} else if (i == stepList.size()-1) { 
				
				//Last, we don't move its destination
				step.setOriginX(step.getOriginX() + quantity);
				
			} else {
				
				step.setDestinationX(step.getDestinationX()+quantity);
				step.setOriginX(step.getOriginX() + quantity);
			}
		}
	}

	private void moveY(List<Step> stepList, int quantity){
		
		for (int i=0; i < stepList.size(); i++) {
			
			Step step = stepList.get(i);
			
			if (i == 0) { //First, we don't move its origin
				
				step.setDestinationY(step.getDestinationY()+quantity);
				
			} else if (i == stepList.size()) { 
				//Last, we don't move its destination			
				step.setOriginY(step.getOriginY() + quantity);
			} else {
				
				step.setDestinationY(step.getDestinationY()+quantity);
				step.setOriginY(step.getOriginY() + quantity);
			}
		}
	}
	
	public Segment getSegmentByID(String id) {
		return segmentsMap.get(id);
	}

	public Map<String, Segment> getSegmentsAux() {
		return segmentsMap;
	}
	
	private List<String> getNewVirtualNodes() {
		return new ArrayList<String>(virtualNodes);
	}
	
	//TODO: Is right to stop when first time a Node ending in destinationID is 
	//      reached or maybe it is better to finish when all the ways to the 
	//      destinationID are reached?
	/**
	 * Implementation of the ShortestPath method by Dijkstra.
	 * @param segmentID The segmentID by which I reach the originID intersection
	 * @param originID The intersectionID representing the departure node
	 * @param destinationID The intersectionID of the destination node
	 * @return An String made up of sequential chunks SegmentID#IntersectionID
	 *         representing the intersections traversed and the segments that 
	 *         connect them.
	 */
	public String DijkstraShortestPath(String segmentID, String originID, 
			                           String destinationID) {
		String initialNode = segmentID + "#" + originID;
		List<String> notYetVisitedNodes = getNewVirtualNodes();
		Map<String, String> previousNode = new HashMap<String, String>();
		Map<String, Double> distanceMin = new HashMap<String, Double>();
		for(String node:notYetVisitedNodes) {
			distanceMin.put(node, Double.MAX_VALUE);
		}
		distanceMin.put(initialNode, 0.0);
		String chosenNode = null;
		while (!notYetVisitedNodes.isEmpty()) {
			// Compute the node with the minimal distance
			int posMin = 0;
			for(int i = 1; i<notYetVisitedNodes.size(); i++)
				if (distanceMin.get(notYetVisitedNodes.get(i)) <
						distanceMin.get(notYetVisitedNodes.get(posMin)))
					posMin = i;
			chosenNode = notYetVisitedNodes.get(posMin);
			String chosenInt = chosenNode.split("#")[1];
			// If the destinationID is reached shortestPath finishes
			if (chosenInt.equals(destinationID)) break;
			for(Segment s:getIntersectionByID(chosenInt).getOutSegments()) {
				String nextNode = s.getId() + "#" + 
			                             s.getDestination().getId();
				if (distanceMin.get(chosenNode) + s.getWeight() < 
					distanceMin.get(nextNode)) {
					distanceMin.put(nextNode, 
							        distanceMin.get(chosenNode) + s.getWeight());
					previousNode.put(nextNode, chosenNode);
				}
			}
			notYetVisitedNodes.remove(posMin);
		}
		//TODO: What would happen if there is not a way to destinationID?
		String pathString = chosenNode;
		while (!previousNode.get(chosenNode).equals(initialNode)) {
			pathString = previousNode.get(chosenNode) + "#" + pathString;
			chosenNode = previousNode.get(chosenNode);
		}
		return pathString;
	}
}
