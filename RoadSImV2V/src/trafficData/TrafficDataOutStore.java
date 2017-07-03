package trafficData;

import java.util.HashMap;

import org.json.JSONObject;

public class TrafficDataOutStore {

	private HashMap<String, TrafficData> data;
	
	public TrafficDataOutStore(){
		data = new HashMap<String, TrafficData>();
	}
	
	public void put(String segment, TrafficData bd) {
		data.put(segment, bd);
	}
	
	public HashMap<String, TrafficData> getData() {
		return data;
	}

	public void setData(HashMap<String, TrafficData> data) {
		this.data = data;
	}

	public void put(String segment, JSONObject json){
		data.put(segment, new TrafficData(json));
	}

	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		for(String key:data.keySet()) {
			json.put(key, data.get(key).toJSON());
		}
		return json;
	}
}
