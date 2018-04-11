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

import it.univaq.au4h.observers.CalibrationCompleteObserver;
import it.univaq.au4h.observers.LostUserObserver;
import it.univaq.au4h.observers.NewUserObserver;
import it.univaq.au4h.observers.PoseDetectedObserver;
import it.univaq.au4h.utility.EventsCapability;

public class NIHelper 
{  
	private OutArg<ScriptNode> scriptNode;
	private Context context;
	
	private DepthGenerator depthGen;
	private DepthMetaData depthMd;
	
	private UserGenerator userGen;
	private SceneMetaData userMd;
	
	private SkeletonCapability skeletonCap;
	private PoseDetectionCapability poseDetectionCap;
	String calibPose = null;
	
	private HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>> joints;
	private HashMap<Integer,EventsCapability> evCaps;

	private final String SAMPLE_XML_FILE = "/home/giovanni/Kinect/OpenNI-master/Data/SamplesConfig.xml";

	private SkeletonGestureHelper skeletonGestureHelper; //gesture detector
	
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
			evCaps=new HashMap<Integer,EventsCapability>();
			
			userGen.getNewUserEvent().addObserver(new NewUserObserver(skeletonCap,poseDetectionCap,calibPose));
			userGen.getLostUserEvent().addObserver(new LostUserObserver(joints, evCaps));
			skeletonCap.getCalibrationCompleteEvent().addObserver(new CalibrationCompleteObserver(skeletonCap,poseDetectionCap,calibPose, joints, evCaps));
			poseDetectionCap.getPoseDetectedEvent().addObserver(new PoseDetectedObserver(skeletonCap,poseDetectionCap));
		
			skeletonCap.setSkeletonProfile(SkeletonProfile.ALL);

			skeletonGestureHelper = new SkeletonGestureHelper(this.joints, this.evCaps);
	}
	
	public ShortBuffer getSceneData() {
		return userMd.getData().createShortBuffer();
	}
	
	public ShortBuffer getDepthData() {
		return depthMd.getData().createShortBuffer();
	}
	
	public SkeletonCapability getSkeletonCap() {
		return skeletonCap;
	}
	
	public EventsCapability checkUserEvents(int userID) {
		return this.skeletonGestureHelper.checkGests(userID);
	}
	
	public int getWidth() {
		return depthMd.getFullXRes();
	}
	
	public int getHeigth() {
		return depthMd.getFullYRes();
	}
	
	public void startGeneratingData() throws StatusException{
		this.context.startGeneratingAll();
	}
	
	public void stopContext() throws StatusException {
		this.context.stopGeneratingAll();
	}
	
	public void waitUpdates() throws StatusException {
		this.context.waitAnyUpdateAll();

	}
	
	public void releaseContext() {
		this.context.release();
	}
	
	public int[] getUsers() throws StatusException {

			return this.userGen.getUsers();
			
	}
	
	public boolean isUserCalibrating(int userId) {
		return this.skeletonCap.isSkeletonCalibrating(userId);
	}
	
	public boolean isUserTracking(int userId) {
		return this.skeletonCap.isSkeletonTracking(userId);
	}
	
	
	private void updateJoint(int user, SkeletonJoint joint) throws StatusException {
		SkeletonJointPosition pos = skeletonCap.getSkeletonJointPosition(user, joint);
		if (pos.getPosition().getZ() != 0) {
			joints.get(user).put(joint, new SkeletonJointPosition(depthGen.convertRealWorldToProjective(pos.getPosition()), pos.getConfidence()));
		}
		else {
			joints.get(user).put(joint, new SkeletonJointPosition(new Point3D(), 0));
		}
	}
	
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

	private void drawLine(Graphics g, HashMap<SkeletonJoint, SkeletonJointPosition> jointHash, SkeletonJoint joint1, SkeletonJoint joint2)
	{
		Point3D pos1 = jointHash.get(joint1).getPosition();
		Point3D pos2 = jointHash.get(joint2).getPosition();
		if (jointHash.get(joint1).getConfidence() == 0 || jointHash.get(joint2).getConfidence() == 0)
			return;
		g.drawLine((int)pos1.getX(), (int)pos1.getY(), (int)pos2.getX(), (int)pos2.getY());
	}
	
	
	
}

