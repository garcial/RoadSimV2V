package routeMethods;

import environment.Segment;
import environment.TrafficMap;
import trafficData.TrafficDataInStore;

public class LessDistanceRoute extends Route {

	@Override
	public void setWeights(TrafficMap tMap, int maxSpeed, TrafficDataInStore futureTraffic) {
		for(Segment s: tMap.getSegments()) {
			s.setWeight(s.getLength());
		}
	}

}
