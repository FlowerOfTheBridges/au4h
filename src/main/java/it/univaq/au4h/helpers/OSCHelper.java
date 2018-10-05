package it.univaq.au4h.helpers;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.illposed.osc.OSCBundle;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortOut;

import it.univaq.au4h.models.Gestures;

/**
 * 
 * @author giovanni
 *
 */
public class OSCHelper{

	private OSCPortOut portOut;
	private static int portNumber=9000;

	private OSCBundle bundle;

	private boolean delay = false;
	private boolean reverbWet = false;
	private boolean reverbRoomsize = false;
	private boolean fuzz = false;
	private boolean trigger= false;

	private final String ROOT = "/kinect";

	private final String DELAY = "/delay";
	private final String FEEDBACK = "/feedback";

	private final String REVERB = "/reverb";
	private final String ROOMSIZE = "/roomsize";
	private final String WET = "/wet";

	private final String FUZZ="/fuzz";

	private final String CONTROL="/control";


	/**
	 * 
	 * @throws SocketException
	 * @throws UnknownHostException
	 */
	public OSCHelper() throws SocketException, UnknownHostException {

		this.portOut=new OSCPortOut(InetAddress.getLocalHost(),portNumber);
	}

	/**
	 * 
	 */
	public void sendOSCBundle(Gestures gests) {
		bundle=new OSCBundle();
		if(this.bindEffectsToGestures(gests)){
			try {
				portOut.send(bundle);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private boolean bindEffectsToGestures(Gestures gests) {
		return (this.bindEffectToGesture(gests, Gestures.Name.OPEN_LEGS, ROOT+REVERB+CONTROL, ROOT+REVERB+WET)||
				this.bindEffectToGesture(gests, Gestures.Name.LSHOULDER_UP, null, ROOT+REVERB+ROOMSIZE)||
				this.bindEffectToGesture(gests, Gestures.Name.LHAND_TO_CAMERA, ROOT+DELAY+CONTROL, ROOT+DELAY+FEEDBACK)||
				this.bindEffectToGesture(gests, Gestures.Name.RHAND_UP, ROOT+FUZZ, null));
	}
	

	private void addMessageToBundle(Object value, String path) {
		OSCMessage msg=new OSCMessage(path);
		msg.addArgument(value);
		bundle.addPacket(msg);
	}

	private boolean bindEffectToGesture(Gestures gests, Gestures.Name name, String controlPath, String measurePath){
		boolean bundleModified=false; // boolean value to check if the bundle has been modified
		if(gests.isGestureActive(name)) { //if the gesture is active...
			
			if(!checkEffect(name)){ //if the effect is disable...
			
				if(controlPath!=null) { //if the control path is specified...
					this.switchEffect(name, true); //...then switch it to active!
					this.addMessageToBundle(this.getEffect(name), controlPath); //...add the effect status to OSC Bundle
					bundleModified = true; //the bundle has been modified
				
				}
			}
			
			if(measurePath!= null){ //if the measure path is specified...
				
				float measure=gests.getMeasureFromGesture(name); //...get the measure from him
				
				this.addMessageToBundle(measure, measurePath); //...then send the gesture's measure to the measure path
				bundleModified = true; //the bundle has been modified
			}

		}
		else{ // ...the gesture is not active

			if(checkEffect(name)) { // if the effect is enabled...
				
				if(controlPath!=null) { //if the control path is specified...
					this.switchEffect(name, false); //...then switch it off
					this.addMessageToBundle(this.getEffect(name), controlPath); // ...then send message to control path
					bundleModified = true; //the bundle has been modified
				}

				if(measurePath!=null) // if the measure path is specified...
				{ 
					this.addMessageToBundle(0f, measurePath); //...send the null measure to OSC Bundle
					bundleModified = true; //the bundle has been modified
				}

			}

		}
		
		return bundleModified;
	}

	
	private void switchEffect(Gestures.Name name, boolean value) {
		if(name.equals(Gestures.Name.LHAND_TO_CAMERA))
			switchDelay(value);
		
		
		if(name.equals(Gestures.Name.RHAND_UP))
			switchFuzz(value);
		
		if(name.equals(Gestures.Name.OPEN_LEGS))
			switchReverbWet(value);
		
		if(name.equals(Gestures.Name.LSHOULDER_UP))
			switchReverbRoomsize(value);
	}
	
	private boolean checkEffect(Gestures.Name name) {
		if(name.equals(Gestures.Name.RHAND_UP))
			return getFuzz();
		
		if(name.equals(Gestures.Name.LHAND_TO_CAMERA))
			return getDelay();
		
		if(name.equals(Gestures.Name.OPEN_LEGS))
			return getReverbWet();
		
		if(name.equals(Gestures.Name.LSHOULDER_UP))
			return getReverbRoomsize();
		
		return false;
		
	}
	
	private boolean getEffect(Gestures.Name name) {
		
		if(name.equals(Gestures.Name.RHAND_UP))
			return getFuzz();
		
		if(name.equals(Gestures.Name.LHAND_TO_CAMERA))
			return getDelay();
		
		if(name.equals(Gestures.Name.OPEN_LEGS))
			return getReverbWet();
		
		if(name.equals(Gestures.Name.LSHOULDER_UP))
			return getReverbRoomsize();
		
		return false;
		
	}
	
	private void switchDelay(boolean value) {
		delay=value;
		
	}

	private void switchReverbWet(boolean value) {
	reverbWet=value;
		
	}
	private void switchReverbRoomsize(boolean value) {
		reverbRoomsize=value;
	}

	private void switchFuzz(boolean value) {

		fuzz = value;
	}

	private boolean getDelay() {
		return delay;
	}

	private boolean getReverbWet() {
		return reverbWet;
	}
	
	private boolean getReverbRoomsize() {
		return reverbRoomsize;
	}

	private boolean getFuzz() {
		return fuzz;
	}
	
}

