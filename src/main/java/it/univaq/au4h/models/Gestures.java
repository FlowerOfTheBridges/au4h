package it.univaq.au4h.models;


import java.util.HashMap;


public class Gestures {
	


	private HashMap<GestureName, Float> gestures;

	public Gestures() {
		gestures = new HashMap<GestureName,Float>();
		for(GestureName g : GestureName.values())
			gestures.put(g, 0f);
	}



	public float getMeasureFromEvent(GestureName gn) {
		return gestures.get(gn);
	}

	public void addMeasureToEvent(GestureName event, float measure) {
		gestures.put(event, measure);
	}

	public boolean isEventActive(GestureName gn) {

		if(gestures.get(gn)!=0f)
			return true;
		else return false;

	}

	public void setEventNull(GestureName event) {
		gestures.put(event, 0f);
	}

}
