package it.univaq.au4h.models;


import java.util.HashMap;


public class Gestures {
	


	private HashMap<GestureName, Float> gestures;

	public Gestures() {
		gestures = new HashMap<GestureName,Float>();
		for(GestureName g : GestureName.values())
			gestures.put(g, 0f);
	}



	public float getMeasureFromGesture(GestureName gn) {
		return gestures.get(gn);
	}

	public void addMeasureToGesture(GestureName event, float measure) {
		gestures.put(event, measure);
	}

	public boolean isGestureActive(GestureName gn) {

		if(gestures.get(gn)!=0f)
			return true;
		else return false;

	}

	public void setGestureNull(GestureName event) {
		gestures.put(event, 0f);
	}

}
