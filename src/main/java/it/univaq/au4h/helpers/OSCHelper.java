package it.univaq.au4h.helpers;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.illposed.osc.OSCBundle;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortOut;

import it.univaq.au4h.models.GestureName;
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

	private Gestures gestures;

	private boolean delay=false;
	private boolean reverb=false;
	private boolean fuzz=false;

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
	public void sendOSCBundle(Gestures gest) {
		this.gestures=gest;
		bundle=new OSCBundle();
		if(this.bindEffectsToGestures()){
			try {
				portOut.send(bundle);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private boolean bindEffectsToGestures() {
		return (this.bindEffectToGesture(GestureName.OPEN_LEGS, ROOT+REVERB+CONTROL, ROOT+REVERB+WET)||
				this.bindEffectToGesture(GestureName.LSHOULDER_UP, null, ROOT+REVERB+ROOMSIZE)||
				this.bindEffectToGesture(GestureName.LHAND_TO_CAMERA, ROOT+DELAY+CONTROL, ROOT+DELAY+FEEDBACK)||
				this.bindEffectToGesture(GestureName.RHAND_UP, ROOT+FUZZ, null));
	}
	
	/*
	private void addControlMessageToBundle(boolean control, String path) {
		OSCMessage msg=new OSCMessage(path);
		msg.addArgument(control);
		bundle.addPacket(msg);
	}

	private void addMeasureMessageToBundle(float measure, String path) {
		OSCMessage msg=new OSCMessage(path);
		msg.addArgument(measure);
		bundle.addPacket(msg);
	}
	
	*/
	
	private void addMessageToBundle(Object value, String path) {
		OSCMessage msg=new OSCMessage(path);
		msg.addArgument(value);
		bundle.addPacket(msg);
	}

	private boolean bindEffectToGesture(GestureName gn, String controlPath, String measurePath){
		boolean bundleModified=false;
		if(gestures.isGestureActive(gn)) {
			float measure=gestures.getMeasureFromGesture(gn);
			if(controlPath!= null){
				if(!checkEffect(gn) || gn.equals(GestureName.RHAND_UP)) {
					this.switchEffect(gn, true);
					this.addMessageToBundle(this.checkEffect(gn), controlPath);
					bundleModified = true;
				}
			}

			if(measurePath!= null){
				this.addMessageToBundle(measure, measurePath);
				bundleModified = true;
			}

		}
		else{

			if(controlPath!=null) {
				if(checkEffect(gn) && !gn.equals(GestureName.RHAND_UP)) {
					switchEffect(gn, false);
					this.addMessageToBundle(false, controlPath);
					bundleModified = true;
				}
			}
		}
		return bundleModified;
	}

	
	private void switchEffect(GestureName gn, boolean value) {
		if(gn.equals(GestureName.LHAND_TO_CAMERA))
			switchDelay(value);
		
		
		if(gn.equals(GestureName.RHAND_UP))
			switchFuzz();
		
		if(gn.equals(GestureName.OPEN_LEGS))
			switchReverb(value);
	}
	
	private boolean checkEffect(GestureName gn) {
		if(gn.equals(GestureName.RHAND_UP))
			return getFuzz();
		
		
		if(gn.equals(GestureName.LHAND_TO_CAMERA))
			return getDelay();
		
		if(gn.equals(GestureName.OPEN_LEGS))
			return getReverb();
		
		return false;
		
	}
	
	private void switchDelay(boolean value) {
		delay=value;
		
	}

	private void switchReverb(boolean value) {
	reverb=value;
		
	}

	private void switchFuzz() {
		if(!fuzz)
			fuzz=true;
		else fuzz=false;
		
	}

	private boolean getDelay() {
		return delay;
	}

	private boolean getReverb() {
		return reverb;
	}

	private boolean getFuzz() {
		return fuzz;
	}
	
}

