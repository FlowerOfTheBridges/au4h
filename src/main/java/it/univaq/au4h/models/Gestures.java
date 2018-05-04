package it.univaq.au4h.models;

import java.util.HashMap;

public class Gestures {

	/**
	 * 
	 * An enum containing all the available gestures. Their recognition is
	 * implemented in the SkeletonGestureHelper class.
	 *
	 */
	public enum Name {
		OPEN_LEGS, 
		LSHOULDER_UP,   
		RHAND_UP,
		LHAND_TO_CAMERA;
	}

	private HashMap<Name, Float> gestures; //the hashmap which associates to a gesture its measure (a Float object)

	/**
	 * Creates a new Gestures object and put 
	 * all the available gestures in it.
	 */
	public Gestures() {
		gestures = new HashMap<Name,Float>();
		for(Name g : Name.values()) //insert all the available gesture names in gestures
			gestures.put(g, 0f);
	}


	/**
	 * 
	 * @param name the gesture name
	 * @return float containing the measure associated to a specific gesture
	 */
	public float getMeasureFromGesture(Name name) {
		return gestures.get(name);
	}

	/**
	 * Associates a measure to a specific gesture.
	 * @param name the gesture's name
	 * @param measure the measure 
	 */
	public void addMeasureToGesture(Name name, float measure) {
		gestures.put(name, measure);
	}

	/**
	 * 
	 * @param name the name of the gesture to check.
	 * @return true if the gesture is active, false otherwise.
	 */
	public boolean isGestureActive(Name name) {

		if(gestures.get(name)!=0f)
			return true;
		else return false;

	}

	/**
	 * Disable a specific gesture.
	 * @param name
	 */
	public void setGestureNull(Name name) {
		gestures.put(name, 0f);
	}

}
