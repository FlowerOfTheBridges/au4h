package it.univaq.au4h.helpers;

import java.awt.Graphics;
import java.nio.ShortBuffer;
import java.util.HashMap;

import org.openni.Context;
import org.openni.DepthGenerator;
import org.openni.DepthMetaData;
import org.openni.GeneralException;
import org.openni.OutArg;
import org.openni.Point3D;
import org.openni.PoseDetectionCapability;
import org.openni.SceneMetaData;
import org.openni.ScriptNode;
import org.openni.SkeletonCapability;
import org.openni.SkeletonJoint;
import org.openni.SkeletonJointPosition;
import org.openni.SkeletonProfile;
import org.openni.StatusException;
import org.openni.UserGenerator;

import it.univaq.au4h.models.Gestures;
import it.univaq.au4h.observers.CalibrationCompleteObserver;
import it.univaq.au4h.observers.LostUserObserver;
import it.univaq.au4h.observers.NewUserObserver;
import it.univaq.au4h.observers.PoseDetectedObserver;

/**
 * The NIHelper class gives the application a way to store and use the
 * visual data generated by the device, thanks to the OpenNI library.
 * @author giovanni
 *
 */
public class NIHelper 
{  
	private OutArg<ScriptNode> scriptNode;
	private Context context; // the status of the application
	
	private DepthGenerator depthGen; // 
	private DepthMetaData depthMd;
	
	private UserGenerator userGen;
	private SceneMetaData userMd;
	
	// attributes 
	private SkeletonCapability skeletonCap;
	private PoseDetectionCapability poseDetectionCap;
	String calibPose = null;
	
	private HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>> joints;
	private HashMap<Integer,Gestures> gestures;

	private final String SAMPLE_XML_FILE = "/home/giovanni/Kinect/OpenNI-master/Data/SamplesConfig.xml";

	private SkeletonGestureHelper skeletonGestureHelper; //gesture detector
	
	/**
	 * The class constructor initializes all the attributes in order to
	 * provide all the data required by the application and give to the 
	 * device all the information for generating all the kind of data. 
	 * @throws GeneralException
	 */
	public NIHelper() throws GeneralException {
		
			scriptNode = new OutArg<ScriptNode>();
			context = Context.createFromXmlFile(SAMPLE_XML_FILE, scriptNode); 
			
			depthGen = DepthGenerator.create(context);
            depthMd = depthGen.getMetaData();
			
            userGen = UserGenerator.create(context);
			userMd=userGen.getUserPixels(0);
			
			skeletonCap = userGen.getSkeletonCapability();
			poseDetectionCap = userGen.getPoseDetectionCapability();
			calibPose = skeletonCap.getSkeletonCalibrationPose();
			joints = new HashMap<Integer, HashMap<SkeletonJoint,SkeletonJointPosition>>();
			gestures=new HashMap<Integer,Gestures>();
			
			userGen.getNewUserEvent().addObserver(new NewUserObserver(skeletonCap,poseDetectionCap,calibPose));
			userGen.getLostUserEvent().addObserver(new LostUserObserver(joints, gestures));
			skeletonCap.getCalibrationCompleteEvent().addObserver(new CalibrationCompleteObserver(skeletonCap,poseDetectionCap,calibPose, joints, gestures));
			poseDetectionCap.getPoseDetectedEvent().addObserver(new PoseDetectedObserver(skeletonCap,poseDetectionCap));
		
			skeletonCap.setSkeletonProfile(SkeletonProfile.ALL);

			skeletonGestureHelper = new SkeletonGestureHelper(this.joints, this.gestures);
	}
	
	/**
	 * 
	 * @return
	 */
	public ShortBuffer getSceneData() {
		return userMd.getData().createShortBuffer();
	}
	
	/**
	 * 
	 * @return
	 */
	public ShortBuffer getDepthData() {
		return depthMd.getData().createShortBuffer();
	}
	
	/**
	 * 
	 * @return
	 */
	public boolean isSkeletonTracking(int user) {
		return skeletonCap.isSkeletonTracking(user);
	}
	
	/**
	 * 
	 * @param userID
	 * @return
	 */
	public Gestures checkUserEvents(int userID) {
		return this.skeletonGestureHelper.checkGests(userID);
	}
	
	/**
	 * 
	 * @return
	 */
	public int getWidth() {
		return depthMd.getFullXRes();
	}
	
	/**
	 * 
	 * @return
	 */
	public int getHeigth() {
		return depthMd.getFullYRes();
	}
	
	/**
	 * 
	 * @throws StatusException
	 */
	public void startGeneratingData() throws StatusException{
		this.context.startGeneratingAll();
	}
	
	/**
	 * 
	 * @throws StatusException
	 */
	public void stopContext() throws StatusException {
		this.context.stopGeneratingAll();
	}
	
	/**
	 * 
	 * @throws StatusException
	 */
	public void waitUpdates() throws StatusException {
		this.context.waitAnyUpdateAll();

	}
	
	/**
	 * 
	 */
	public void releaseContext() {
		this.context.release();
	}
	
	/**
	 * 
	 * @return the number of users in the scene
	 * @throws StatusException
	 */
	public int[] getUsers() throws StatusException {

			return this.userGen.getUsers();
			
	}
	

	/**
	 * Method that checks whenever a user is in the calibration mode 
	 * @param userId the user to control
	 * @return true if the user is in calibration mode, false otherwise
	 */
	public boolean isUserCalibrating(int userId) {
		return this.skeletonCap.isSkeletonCalibrating(userId);
	}
	
	/**
	 * Method that checks whenever a user is in the tracking mode 
	 * @param userId the user to control
	 * @return true if the user is in tracking mode, false otherwise
	 */
	public boolean isUserTracking(int userId) {
		return this.skeletonCap.isSkeletonTracking(userId);
	}
	
	/**
	 * Method that updates a particular joint of a user on the scene.
	 * If joint is not found, a null point will be added.
	 * @param user the user identifier 
	 * @param joint the particular joint to update
	 * @throws StatusException
	 */
	private void updateJoint(int user, SkeletonJoint joint) throws StatusException {
		SkeletonJointPosition pos = skeletonCap.getSkeletonJointPosition(user, joint);
		if (pos.getPosition().getZ() != 0) {
			joints.get(user).put(joint, new SkeletonJointPosition(depthGen.convertRealWorldToProjective(pos.getPosition()), pos.getConfidence()));
		}
		else {
			joints.get(user).put(joint, new SkeletonJointPosition(new Point3D(), 0));
		}
	}
	
	/**
	 * Method that updates all the joints of a particular user.
	 * @param user
	 * @throws StatusException
	 */
	public void updateJoints(int user) throws StatusException {
		updateJoint(user, SkeletonJoint.HEAD);
		updateJoint(user, SkeletonJoint.NECK);

		updateJoint(user, SkeletonJoint.LEFT_SHOULDER);
		updateJoint(user, SkeletonJoint.LEFT_ELBOW);
		updateJoint(user, SkeletonJoint.LEFT_HAND);

		updateJoint(user, SkeletonJoint.RIGHT_SHOULDER);
		updateJoint(user, SkeletonJoint.RIGHT_ELBOW);
		updateJoint(user, SkeletonJoint.RIGHT_HAND);

		updateJoint(user, SkeletonJoint.TORSO);

		updateJoint(user, SkeletonJoint.LEFT_HIP);
		updateJoint(user, SkeletonJoint.LEFT_KNEE);
		updateJoint(user, SkeletonJoint.LEFT_FOOT);

		updateJoint(user, SkeletonJoint.RIGHT_HIP);
		updateJoint(user, SkeletonJoint.RIGHT_KNEE);
		updateJoint(user, SkeletonJoint.RIGHT_FOOT);
	}
	
	/**
	 * Method that draw the skeleton line in the frame.
	 * @param g
	 * @param user
	 */
	public void drawSkeleton(Graphics g, int user)
	{
		try {
			updateJoints(user);
		} catch (StatusException e) {
			e.printStackTrace();
		}
		
		HashMap<SkeletonJoint, SkeletonJointPosition> dict = joints.get(new Integer(user));

		drawLine(g, dict, SkeletonJoint.HEAD, SkeletonJoint.NECK);

		drawLine(g, dict, SkeletonJoint.LEFT_SHOULDER, SkeletonJoint.TORSO);
		drawLine(g, dict, SkeletonJoint.RIGHT_SHOULDER, SkeletonJoint.TORSO);

		drawLine(g, dict, SkeletonJoint.NECK, SkeletonJoint.LEFT_SHOULDER);
		drawLine(g, dict, SkeletonJoint.LEFT_SHOULDER, SkeletonJoint.LEFT_ELBOW);
		drawLine(g, dict, SkeletonJoint.LEFT_ELBOW, SkeletonJoint.LEFT_HAND);

		drawLine(g, dict, SkeletonJoint.NECK, SkeletonJoint.RIGHT_SHOULDER);
		drawLine(g, dict, SkeletonJoint.RIGHT_SHOULDER, SkeletonJoint.RIGHT_ELBOW);
		drawLine(g, dict, SkeletonJoint.RIGHT_ELBOW, SkeletonJoint.RIGHT_HAND);

		drawLine(g, dict, SkeletonJoint.LEFT_HIP, SkeletonJoint.TORSO);
		drawLine(g, dict, SkeletonJoint.RIGHT_HIP, SkeletonJoint.TORSO);
		drawLine(g, dict, SkeletonJoint.LEFT_HIP, SkeletonJoint.RIGHT_HIP);

		drawLine(g, dict, SkeletonJoint.LEFT_HIP, SkeletonJoint.LEFT_KNEE);
		drawLine(g, dict, SkeletonJoint.LEFT_KNEE, SkeletonJoint.LEFT_FOOT);

		drawLine(g, dict, SkeletonJoint.RIGHT_HIP, SkeletonJoint.RIGHT_KNEE);
		drawLine(g, dict, SkeletonJoint.RIGHT_KNEE, SkeletonJoint.RIGHT_FOOT);
	}

	/**
	 * 
	 * @param g
	 * @param jointHash
	 * @param joint1
	 * @param joint2
	 */
	private void drawLine(Graphics g, HashMap<SkeletonJoint, SkeletonJointPosition> jointHash, SkeletonJoint joint1, SkeletonJoint joint2)
	{
		Point3D pos1 = jointHash.get(joint1).getPosition();
		Point3D pos2 = jointHash.get(joint2).getPosition();
		if (jointHash.get(joint1).getConfidence() == 0 || jointHash.get(joint2).getConfidence() == 0)
			return;
		g.drawLine((int)pos1.getX(), (int)pos1.getY(), (int)pos2.getX(), (int)pos2.getY());
	}
	
	
	
}

