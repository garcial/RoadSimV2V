package routeMethods;

import java.awt.Color;

/**
 * Enumeration for the different types of algorithms
 * Two arguments are provided: the int representing the related integer value
 *   and a Color value for showing in the ongoing simulation window. 
 */
public enum Method {

	LESSDISTANCE(0, Color.green), FASTBLIND(1, Color.blue), 
	SMARTLASTUPDATE(2, Color.red);
	
    public final int value;
    public final Color color;

    private Method(int value, Color color) {
        this.value = value;
        this.color = color;
    }
}