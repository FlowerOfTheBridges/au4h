package it.univaq.au4h.utility;


import java.util.HashMap;


public class EventsCapability {
	
	private HashMap<GestureName, Float> events;
	
	public EventsCapability() {
		events = new HashMap<GestureName,Float>();
	}
	

	
	public float getMeasureFromEvent(GestureName gn) {
		return events.get(gn);
	}
	
	public void addMeasureToEvent(GestureName event, float measure) {
		events.put(event, measure);
	}
	
	public boolean isEventActive(GestureName gn) {
		if(!events.isEmpty()) {
			if(events.containsKey(gn) && events.get(gn)!=0f){
				return true;
			}
			else{
				return false;
			}
		}
		else {
			return false;
		}
			
	}
	
	public void setEventNull(GestureName event) {
		events.put(event, 0f);
	}

}
