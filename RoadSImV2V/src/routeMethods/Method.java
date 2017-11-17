package routeMethods;

/**
 * Enumeration for the different types of algorithms
 *	TODO: Make differences between Startsmart , dynamicsmart and dijkstra
 */
public enum Method {

	LESSDISTANCE(0), FASTBLIND(1), SMARTLASTUPDATE(2);
	
    public final int value;

    private Method(int value) {
        this.value = value;
    }
}