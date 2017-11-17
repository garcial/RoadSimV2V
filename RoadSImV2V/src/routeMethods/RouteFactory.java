package routeMethods;

public class RouteFactory {

	public Route getRoute(Method myMethod) {

		if (myMethod == Method.FASTBLIND)
			return new FastBlindRoute();
		else if (myMethod == Method.LESSDISTANCE)
			return new LessDistanceRoute();
		else // The car is a smart one
			return new SmartLastUpdateRoute();
	}
}
