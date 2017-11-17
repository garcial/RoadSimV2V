package routeMethods;

import java.util.ArrayList;
import java.util.Map;

import environment.Segment;
import environment.TrafficMap;
import trafficData.TrafficData;
import trafficData.TrafficDataInStore;

public class SmartLastUpdateRoute extends Route {

	@Override
	public void setWeights(TrafficMap tMap, int maxSpeed, 
			               TrafficDataInStore futureTraffic) {
		Map<String, ArrayList<TrafficData>> data = futureTraffic.getData();
		for(String s: data.keySet()) {
			TrafficData closest = data.get(s).get(0);
			for(int i = 1; i < data.get(s).size(); i++) {
				if (closest.getTfin() < data.get(s).get(i).getNumCars())
					closest = data.get(s).get(i);
			}
			Segment segment = tMap.getSegmentByID(s);
			segment.setWeight(closest.getNumCars()/segment.getLength());
		}

	}

}
