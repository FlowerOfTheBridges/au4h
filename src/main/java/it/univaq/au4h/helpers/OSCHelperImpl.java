package it.univaq.au4h.helpers;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import com.illposed.osc.OSCBundle;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortOut;

import it.univaq.au4h.utility.EventsCapability;
import it.univaq.au4h.utility.GestureName;

public class OSCHelperImpl implements OSCHelper{

	private OSCPortOut portOut;
	private static int portNumber=9000;

	private OSCBundle bundle;

	private EventsCapability evCap;

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


	public OSCHelperImpl() throws SocketException, UnknownHostException {

		this.portOut=new OSCPortOut(InetAddress.getLocalHost(),portNumber);
	}

	public void sendOSCBundle(EventsCapability ev) {
		this.evCap=ev;
		bundle=new OSCBundle();
		if(this.bindEffectToGesture(GestureName.OPEN_LEGS, reverb, ROOT+REVERB+CONTROL, ROOT+REVERB+WET)||
				this.bindEffectToGesture(GestureName.LEAN_BACK, reverb, ROOT+REVERB+CONTROL, ROOT+REVERB+ROOMSIZE)||
				this.bindEffectToGesture(GestureName.NECK_CAMERA, delay, ROOT+DELAY+CONTROL, ROOT+DELAY+FEEDBACK)||
				this.bindEffectToGesture(GestureName.RH_UP, fuzz, ROOT+FUZZ, null)){
			try {
				portOut.send(bundle);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

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
	
	private boolean bindEffectToGesture(GestureName gn, boolean effect, String controlPath, String measurePath){
		boolean bundleModified=false;
		if(evCap.isEventActive(gn)) {
			float measure=evCap.getMeasureFromEvent(gn);
			if(measure!= 0f) {
				if(!effect) {
					effect = true;
					this.addControlMessageToBundle(effect, controlPath);
					bundleModified = true;
				}
				if(measurePath!= null){
					this.addMeasureMessageToBundle(measure, measurePath);
					bundleModified = true;
				}

			}
			else{
				effect = false;
				this.addControlMessageToBundle(effect, controlPath);
				bundleModified = true;
			}
		}
		return bundleModified;
	}
	
}


