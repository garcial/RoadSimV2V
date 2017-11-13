package environment;

import java.io.Serializable;
import java.util.ArrayList; 
import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * Represents the union of several {@link Segment}. A roundabout, 
 * a crossroad, a change of route...
 */
public class Intersection implements Serializable{
	

	private static final long serialVersionUID = 3885620456847709732L;

	//Unique id
	private String id;
	
	//In segmentsTest
	private ArrayList<Segment> in;
	
	//Out segmentsTest
	private ArrayList<Segment> out;
	
	//Every way has a number of ways to exit
	private Map<String, List<String>> allowedWays = new HashMap<String, List<String>>();
	
	//Coordinates
	private int x, y;
	
	/**
	 * Default constructor. 
	 */
	public Intersection(){
		this.id = "";
		this.in = new ArrayList<Segment>();
		this.out = new ArrayList<Segment>();
		this.x = 0;
		this.y = 0;
		this.allowedWays = new HashMap<String, List<String>>();
	}
	
	/**
	 * Constructor. 
	 *
	 * @param  in   A list of {@link Segment} that go into this
	 *              intersection.
	 * @param  out  A list of {@link Segment} that leave this
	 *              intersection.
	 * @param coordinates A array with the coordinates.
	 */
//	public Intersection(String id, ArrayList<Segment> in, 
//			            ArrayList<Segment> out, int x, int y) {
//		
//		this.id = id;
//		this.in = in;
//		this.out = out;
//		this.x = x;
//		this.y = y;
//		for(Segment s: in){
//			//Now we use all the ways in every entry
//			this.allowedRoads.put(s, out);
//		}
//	}
	
	/**
	 * Constructor. 
	 *
	 * @param  in   A list of {@link Segment} that go into this
	 *              intersection.
	 * @param  out  A list of {@link Segment} that leave this 
	 *              intersection.
	 * @param x A double with the x coordinate.
	 * @param y A double with the y coordinate.
	 */
	public Intersection(String id, int x, int y){
		
		this.id = id;
		this.in = new ArrayList<Segment>();
		this.out = new ArrayList<Segment>();
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Adds an in {@link Segment} to the Intersection. 
	 *
	 * @param  segment  {@link Segment} to be added.
	 * @return A boolean whether the collection has been modified 
	 *         or not.
	 */
	public boolean addInSegment(Segment segment){
		
		return this.in.add(segment);
	}
	
	/**
	 * Adds an out {@link Segment} to the Intersection. 
	 *
	 * @param  segment  {@link Segment} to be added.
	 * @return A boolean whether the {@link Segment} has been 
	 *         added or not.
	 */
	public boolean addOutSegment(Segment segment) {
		
		return this.out.add(segment);
	}

	//Setters and getters
	public String getId() {
		return id;
	}

	public ArrayList<Segment> getInSegments() {
		return in;
	}

	public ArrayList<Segment> getOutSegments() {
		return out;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
/**
 * Add a right way from an input segmentID to an output segmentID 
 * into the allowedEWays dictionary of this Intersection
 * @param source The input segmentID
 * @param target The output segmentID
 */
	public void addAllowedWay(String source, String target) {

		if(!this.allowedWays.containsKey(source)){
			ArrayList<String> aux = new ArrayList<String>();
			aux.add(target);
			this.allowedWays.put(source, aux);
		} else {
			this.allowedWays.get(source).add(target);
		}
	}
	
	public void removeAllowedWay(String source, String target) {
		this.allowedWays.get(source).remove(target);
	}
	
	/**
	 * Compute the list of the output segment's ids
	 * @param source source segment id
	 * @return The list with the allowed segment's ids
	 * 
	 */
	public List<String> getAllowedSegments(String source){
		List<String> segments = new ArrayList<String>();
		if(this.allowedWays.get(source) != null){
			segments = this.allowedWays.get(source); 
		}
		return segments;
	}
	
	@Override
	public String toString() {
		return "Intersection [id=" + id + ", in=" + in + ", out=" + out + ", x=" + x + ", y=" + y + "]";
	}	
	
	
}
