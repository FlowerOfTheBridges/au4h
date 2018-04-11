package it.univaq.au4h.controllers;


import java.awt.Component;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.openni.GeneralException;
import org.openni.StatusException;

import it.univaq.au4h.helpers.DrawHelper;
import it.univaq.au4h.helpers.NIHelper;
import it.univaq.au4h.helpers.OSCHelper;
import it.univaq.au4h.helpers.OSCHelperImpl;
import it.univaq.au4h.utility.EventsCapability;

@SuppressWarnings("serial")
public class MainController {

	private NIHelper niHelper;
	private DrawHelper drawHelper;
	private OSCHelper oscHelper;

	public MainController() throws GeneralException, SocketException, UnknownHostException {

		niHelper = new NIHelper();
		
		drawHelper = new DrawHelper(niHelper);
		
		oscHelper = new OSCHelperImpl();

		niHelper.startGeneratingData();

	}

	/**
	 * update and display the users-coloured depth image and skeletons
	 * whenever the context is updated.
	 * @throws StatusException 
	 */
	public void run() throws StatusException{
		boolean isRunning = true;

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
	
	public void update() throws StatusException
	{

		int[] userIDs = niHelper.getUsers();   // there may be many users in the scene
		for (int i = 0; i < userIDs.length; ++i) {
			int userID = userIDs[i];
			if(niHelper.isUserCalibrating(userID))
				continue;
			if (niHelper.isUserTracking(userID))  // test to avoid occassional crashes
				try {
					niHelper.updateJoints(userID);
					EventsCapability userEvCap=niHelper.checkUserEvents(userID);
					if(userEvCap!=null)
						oscHelper.sendOSCBundle(userEvCap);
				} catch (StatusException e) {
					e.printStackTrace();
				}
		}
	}

}
