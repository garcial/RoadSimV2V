package routeMethods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import environment.Segment;
import environment.TrafficMap;
import trafficData.TrafficDataInStore;

public abstract class Route {	
	
	//This is the way weights from futureTrafficData are stated
	public abstract void setWeights(TrafficMap tMap, int maxSpeed,
			                        TrafficDataInStore futureTraffic);

	
	/**
	 * Implementation of the ShortestPath method by Dijkstra.
	 * @param segmentID The segmentID by which I reach the originID intersection
	 * @param originID The intersectionID representing the departure node
	 * @param destinationID The intersectionID of the destination node
	 * @return An String made up of sequential chunks SegmentID#IntersectionID
	 *         representing the intersections traversed and the segments that 
	 *         connect them.
	 */
	public String[] DijkstraShortestPath(TrafficMap tMap, String segmentID,
										 String originID, String destinationID,
										 int maxSpeed,
										 TrafficDataInStore futureTraffic) {

		setWeights(tMap, maxSpeed, futureTraffic);
		List<String> notYetVisitedNodes = tMap.getNewVirtualNodes();
		Map<String, String> previousNode = new HashMap<String, String>();
		Map<String, Double> distanceMin = new HashMap<String, Double>();
		
		String initialNode;
		if (segmentID == null) {
			initialNode = "noSegment#" + originID;
			notYetVisitedNodes.add(initialNode);
		} else initialNode = segmentID + "#" + originID;

		for(String node:notYetVisitedNodes) {
			distanceMin.put(node, Double.MAX_VALUE);
		}
        distanceMin.put(initialNode, 0.0);
        
		// Store all the final virtual nodes related to destinationID
		List<String> finalVirtualNodes = new ArrayList<String>();
		for(Segment s: tMap.getIntersectionByID(destinationID).getInSegments())
			finalVirtualNodes.add(s.getId()+ "#" + destinationID);
		int howManyFinaVirtuallNodes = finalVirtualNodes.size();
		int finalVirtualNodesReached = 0;
        
		String chosenNode = null;
		while (!notYetVisitedNodes.isEmpty()) {
			// Compute the node with the minimal distance
			int posMin = computeMinPosition(notYetVisitedNodes, distanceMin);
			chosenNode = notYetVisitedNodes.get(posMin);
			String chosenInt = chosenNode.split("#")[1];
			// If the destinationID is reached one way to reach destinationID has
			//   been reached. If all ways are computed while loop must finish.
			if (chosenInt.equals(destinationID)) {
				finalVirtualNodesReached++;
				if (finalVirtualNodesReached == howManyFinaVirtuallNodes) break;
			}
			for(Segment s:tMap.getIntersectionByID(chosenInt).getOutSegments()) {
				String nextNode = s.getId() + "#" + 
			                             s.getDestination().getId();
				double newDistance = distanceMin.get(chosenNode) + s.getWeight();
				if (newDistance < distanceMin.get(nextNode)) {
					distanceMin.put(nextNode, newDistance);
					previousNode.put(nextNode, chosenNode);
				}
			}
			notYetVisitedNodes.remove(posMin);
		}
		//TODO: What would happen if there is not a way to destinationID?
		int posMin = computeMinPosition(finalVirtualNodes, distanceMin);
		chosenNode = finalVirtualNodes.get(posMin);
		StringBuilder pathString = new StringBuilder(chosenNode);
		while (!previousNode.get(chosenNode).equals(initialNode)) {
			pathString.append('#');
			pathString.append(previousNode.get(chosenNode));
			chosenNode = previousNode.get(chosenNode);
		}
		String[] pathStringArray = pathString.toString().split("#");
		List<String> list = Arrays.asList(pathStringArray);
		Collections.reverse(list);
		return (String[]) list.toArray();
	}
	
	private int computeMinPosition(List<String> list, Map<String, Double> distances) {
		int posMin = 0;
		for(int i = 1; i<list.size(); i++)
			if (distances.get(list.get(i)) <
					distances.get(list.get(posMin)))
				posMin = i;
		return posMin;
	}

}
