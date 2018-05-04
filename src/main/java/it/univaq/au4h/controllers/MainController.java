package it.univaq.au4h.controllers;


import java.awt.Component;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.openni.GeneralException;
import org.openni.StatusException;

import it.univaq.au4h.helpers.DrawHelper;
import it.univaq.au4h.helpers.NIHelper;
import it.univaq.au4h.helpers.OSCHelper;
import it.univaq.au4h.models.Gestures;


public class MainController {

	private NIHelper niHelper; // gets data from the device
	private DrawHelper drawHelper; // draw data obtained from niHelper to frame.
	private OSCHelper oscHelper; //connects to the audio server and sends data via OSC protocol
	
	private boolean isRunning=true; //boolean to check wheter the app is running or not

	/**
	 * Creates the app enviroment.
	 * @throws GeneralException if device is not ready
	 * @throws SocketException if osc port is unavailable
	 * @throws UnknownHostException if server's host is unavailable
	 */
	public MainController() throws GeneralException, SocketException, UnknownHostException {

		niHelper = new NIHelper();

		drawHelper = new DrawHelper(niHelper);

		oscHelper = new OSCHelper();

		niHelper.startGeneratingData();

	}

	/**
	 * update and display the users-colored depth image and skeletons
	 * whenever the context is updated.
	 * @throws StatusException 
	 */
	public void run() throws StatusException{

		while (isRunning) {

			niHelper.waitUpdates();
			this.update();

			drawHelper.updateDepth();
			drawHelper.repaint();
		}
		niHelper.stopContext();
		niHelper.releaseContext();

		System.exit(0);
	}

	public Component getComponent() {
		return drawHelper;
	}
	
	public void stop() {
		this.isRunning=false;
	}

	/**
	 * Updates user's joints, check current available gestures and sends
	 * their measures to server with OSC.
	 * @throws StatusException
	 */
	private void update() throws StatusException{
		int[] userIDs = niHelper.getUsers();   // there may be many users in the scene
		for (int i = 0; i < userIDs.length; ++i) {
			int userID = userIDs[i];
			if(niHelper.isUserCalibrating(userID)) // test to avoid occasional crashes
				continue;
			if (niHelper.isUserTracking(userID))  
				try {
					niHelper.updateJoints(userID);
					Gestures userGestures=niHelper.checkUserGestures(userID);
					if(userGestures!=null) {
						oscHelper.sendOSCBundle(userGestures);
					}
				} catch (StatusException e) {
					e.printStackTrace();
				}
		}
	}

}
