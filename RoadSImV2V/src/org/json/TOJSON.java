package org.json;

public class TOJSON {

	public TOJSON() {
		// TODO Auto-generated constructor stub
	}
	
	private String toJSon(Object...objects) {
        StringBuilder cadena = new StringBuilder("{");
        for (int i = 0; i < objects.length; i+=2) {
            cadena.append(objects[i].toString()).
                   append(":").
                   append(objects[i+1]).
                   append(",");
        }
        return cadena.append("}").toString();
    }
    
    private String toJSonList(Object...objects) {
        StringBuilder build = new StringBuilder("[");
        for(int i = 0; i< objects.length; i++) 
            build.append(objects[i]).append(",");
        return build.append("]").toString();
    }
}
