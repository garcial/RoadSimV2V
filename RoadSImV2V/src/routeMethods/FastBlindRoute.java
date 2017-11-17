package routeMethods;

import environment.Segment;
import environment.TrafficMap;
import trafficData.TrafficDataInStore;

public class FastBlindRoute extends Route {

	@Override
	public void setWeights(TrafficMap tMap, int maxSpeed, 
			               TrafficDataInStore futureTraffic) {
		for(Segment s: tMap.getSegments()) {
			s.setWeight(maxSpeed < s.getMaxSpeed() ? 
					       s.getLength()/maxSpeed:
					       s.getLength()/s.getMaxSpeed());
		}
	}

}
