package features;

import org.json.JSONObject;

public class CarData {
	private String id; // The getName() of the carAgent
	private float x, y;
	private float currentSpeed;
	private int typeOfAlgorithm;
	private int radio;
	private int maxSpeed;

	public CarData(String id, float x, float y, float currentSpeed,
			       int typeOfAlgorithm, float segmentDistanceCovered,
			       int radio) {
		super();
		this.id = id;
		this.x = x;
		this.y = y;
		this.currentSpeed = currentSpeed;
		this.typeOfAlgorithm = typeOfAlgorithm;
		this.radio = radio;
	}

	public CarData() {
		// TODO Auto-generated constructor stub
	}

	public String getId() {
		return id;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getCurrentSpeed() {
		return currentSpeed;
	}

	public void setCurrentSpeed(float currentSpeed) {
		this.currentSpeed = currentSpeed;
	}

	public int getTypeOfAlgorithm() {
		return typeOfAlgorithm;
	}

	public int getRadio() {
		return radio;
	}

	public void setRadio(int radio) {
		this.radio = radio;
	}

	public JSONObject toJSON() {
		JSONObject resultado =  new JSONObject();
		resultado.put("id", getId());
		resultado.put("x", getX());
		resultado.put("y", getY());
		resultado.put("speed", getCurrentSpeed());
		resultado.put("maxSpeed", getMaxSpeed());
		resultado.put("type", getTypeOfAlgorithm());
		resultado.put("radio", getRadio());
		return resultado;
	}

	public int getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(int maxSpeed) {
		this.maxSpeed = maxSpeed;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setTypeOfAlgorithm(int typeOfAlgorithm) {
		this.typeOfAlgorithm = typeOfAlgorithm;
	}

}