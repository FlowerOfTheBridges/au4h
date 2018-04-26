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

	private NIHelper niHelper;
	private DrawHelper drawHelper;
	private OSCHelper oscHelper;

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
	public void run(boolean isRunning) throws StatusException{

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

	private void update() throws StatusException{
		int[] userIDs = niHelper.getUsers();   // there may be many users in the scene
		for (int i = 0; i < userIDs.length; ++i) {
			int userID = userIDs[i];
			if(niHelper.isUserCalibrating(userID)) // test to avoid occassional crashes
				continue;
			if (niHelper.isUserTracking(userID))  
				try {
					niHelper.updateJoints(userID);
					Gestures userGestures=niHelper.checkUserEvents(userID);
					if(userGestures!=null)
						oscHelper.sendOSCBundle(userGestures);
				} catch (StatusException e) {
					e.printStackTrace();
				}
		}
	}

}
