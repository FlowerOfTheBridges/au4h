/**
 * Basic gestures detector.
 * Examine a user's skeleton to see if it is just starting or stopping
 * any of the gestures in GestureName. If it is then the watcher is notified.
 * The notification consists of calling GesturesWatcher.pose() with the userID,
 * the GestureName value, and a boolean denoted if the gesture has just started
 * or finished.
 */

package it.univaq.au4h.helpers;


import java.util.HashMap;
import org.openni.Point3D;
import org.openni.SkeletonJoint;
import org.openni.SkeletonJointPosition;

import it.univaq.au4h.models.Gestures;

public class SkeletonGestureHelper
{

	private HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>> userSkels;
	private HashMap<Integer, Gestures> userGestures;

	/**
	 * Creates a new SkeletonGestureHelper.
	 * @param uSkels the user skeletal
	 * @param uGests the user Gestures
	 */
	public SkeletonGestureHelper(HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>> uSkels,HashMap<Integer, Gestures> uGests){  
		this.userSkels = uSkels;
		this.userGestures=uGests;
	} 

	/**
	 * Checks which gesture have just started or finished, and
	 * eventually add its measure to the user's Gestures.
	 * @param userID the user to analyze
	 * @return the Gestures of the analyzed user.
	 */
	public Gestures checkGests(int userID){
		HashMap<SkeletonJoint, SkeletonJointPosition> skel = userSkels.get(userID);
		Gestures gestures=userGestures.get(userID);
		if (skel != null && gestures != null) {
			leftArmUp(skel, gestures);
			rightHandUp(skel, gestures);
			openLegs(skel, gestures);
			leftHandToCamera(skel, gestures);
		}
		return this.userGestures.get(userID);
	} 

	private float distApart(Point3D p1, Point3D p2){

		float dist = (float) Math.sqrt( 
				(p1.getX() - p2.getX())*(p1.getX() - p2.getX()) +
				(p1.getY() - p2.getY())*(p1.getY() - p2.getY()));
		return dist;
	}  

	/**
	 * Calculates the measure of the angle between three points.
	 * @param p1 the first point. It's the vertex.
	 * @param p2 the second point.
	 * @param p3 the third point.
	 * @return float corresponding to the angle between the vertex and the other joints 
	 */
	private float angleSkelJoints(Point3D p1, Point3D p2, Point3D p3){
		javafx.geometry.Point3D vertex = new javafx.geometry.Point3D(p1.getX(), p1.getY(), p1.getZ());
		javafx.geometry.Point3D aux1 = new javafx.geometry.Point3D(p2.getX(), p2.getY(), p2.getZ());
		javafx.geometry.Point3D aux2 = new javafx.geometry.Point3D(p3.getX(), p3.getY(), p3.getZ());

		return (float) vertex.angle(aux1, aux2);

	}


	/**
	 * Check if the left hand is heading to camera center.
	 * @param skel the user skeletal
	 * @param gestures 
	 */
	private void leftHandToCamera(HashMap<SkeletonJoint, SkeletonJointPosition> skel, Gestures gestures){
		Point3D leftHand=getJointPos(skel, SkeletonJoint.LEFT_HAND);
		Point3D leftShoulder=getJointPos(skel,SkeletonJoint.LEFT_SHOULDER);
		Point3D rightHand = getJointPos(skel, SkeletonJoint.RIGHT_HAND);
		if(leftHand!=null && leftShoulder!=null && rightHand!=null){
			float angle=this.angleSkelJoints(leftShoulder, new Point3D(leftHand.getX(),leftHand.getY(),leftShoulder.getZ()), leftHand);
			boolean condition=leftHand.getZ()<rightHand.getZ()-(rightHand.getZ())/20;
			this.storeGesture(condition,Gestures.Name.LHAND_TO_CAMERA,angle,gestures);
		}	
	}

	/**
	 * Check if the distance between user's legs is bigger than the shoulder's one.
	 * @param skel the skeleton to use for obtaining the joints position
	 * @param gestures the Gestures Object that will contain the gesture's measure, only if the condition is respected.
	 */
	private void openLegs(HashMap<SkeletonJoint, SkeletonJointPosition> skel, Gestures gestures){
		Point3D lkPt=getJointPos(skel, SkeletonJoint.LEFT_KNEE);
	    Point3D rkPt=getJointPos(skel, SkeletonJoint.RIGHT_KNEE);
	    Point3D lhPt=getJointPos(skel, SkeletonJoint.LEFT_HIP);
	    Point3D rhPt=getJointPos(skel, SkeletonJoint.RIGHT_HIP);
		Point3D torso=getJointPos(skel, SkeletonJoint.TORSO);
		
		Point3D shoulderRPt = getJointPos(skel, SkeletonJoint.RIGHT_SHOULDER);
		Point3D shoulderLPt = getJointPos(skel, SkeletonJoint.LEFT_SHOULDER);

		if(lkPt!=null && rkPt!=null && lhPt!=null && rhPt!=null && shoulderRPt!=null  && shoulderLPt!=null) {
			Point3D middle=new Point3D((lhPt.getX()+rhPt.getX())/2,(lhPt.getY()+rhPt.getY())/2,(lhPt.getZ()+rhPt.getZ())/2);
			float degree=angleSkelJoints(middle,lkPt,rkPt);
			boolean condition=distApart(lkPt,rkPt)>distApart(shoulderRPt, shoulderLPt);
			this.storeGesture(condition, Gestures.Name.OPEN_LEGS, degree, gestures);
		}

	}


	/**
	 * Check if the left arm is upon the left shoulder. 
	 * @param skel the user skeletal
	 * @param gestures the Gestures object of the selected user
	 */
	private void leftArmUp(HashMap<SkeletonJoint, SkeletonJointPosition> skel, Gestures gestures) {
		Point3D lhandPt=getJointPos(skel,SkeletonJoint.LEFT_HAND);
		Point3D lshoulderPt = getJointPos(skel, SkeletonJoint.NECK);
		Point3D lhPt=getJointPos(skel, SkeletonJoint.LEFT_HIP);
		Point3D rhPt=getJointPos(skel, SkeletonJoint.RIGHT_HIP);

		if ((lhandPt!= null) && (lshoulderPt!=null) && (lhPt!=null) && (rhPt!=null)) {
			float degree=angleSkelJoints(lhPt,lhandPt,new Point3D(lhandPt.getX(),lhPt.getY(),lhPt.getZ()));
			boolean condition = lhandPt.getY() < lshoulderPt.getY();
			this.storeGesture(condition, Gestures.Name.LSHOULDER_UP, degree, gestures);
		}
	}

	/**
	 * Checks if the right hand is upon the neck
	 * @param skel
	 * @param gestures
	 */
	private void rightHandUp(HashMap<SkeletonJoint, SkeletonJointPosition> skel, Gestures gestures){
		Point3D rightHandPt = getJointPos(skel, SkeletonJoint.RIGHT_HAND);
		Point3D headPt = getJointPos(skel, SkeletonJoint.HEAD);
		if ((rightHandPt != null) && (headPt != null))
		{
			boolean condition = rightHandPt.getY() <= headPt.getY();
			if(condition)
			{
				if(gestures.getMeasureFromGesture(Gestures.Name.RHAND_UP)!=3f && gestures.getMeasureFromGesture(Gestures.Name.RHAND_UP)!=2f)
					this.storeGesture(condition, Gestures.Name.RHAND_UP, 1f, gestures);
				if(gestures.getMeasureFromGesture(Gestures.Name.RHAND_UP)==3f)
					gestures.addMeasureToGesture(Gestures.Name.RHAND_UP, 2f);
			}
			else
			{
				if(gestures.getMeasureFromGesture(Gestures.Name.RHAND_UP)==1f)
					gestures.addMeasureToGesture(Gestures.Name.RHAND_UP, 3f);
				
				if(gestures.getMeasureFromGesture(Gestures.Name.RHAND_UP)==2f)
					this.storeGesture(condition, Gestures.Name.RHAND_UP, 0f, gestures);
			}
		}
	}  

	/**
	 * Returns the JointPosition 
	 * @param skel the structure containing the joints.
	 * @param j the required joint.
	 * @return Point3D reflecting the joint position in the scene.
	 */
	private Point3D getJointPos(HashMap<SkeletonJoint, SkeletonJointPosition> skel, 
			SkeletonJoint j)
	{
		SkeletonJointPosition pos = skel.get(j);
		if (pos == null) //drops all joints not available
			return null;

		if (pos.getConfidence() <= 0.5) //drops all the joints with low confidence
			return null;

		return pos.getPosition();
	}  

	/**
	 * Associates a measure to its gesture.
	 * @param condition the result of the condition to be verified.
	 * @param gn the gesture's name
	 * @param measure the measure to be stored
	 * @param gestures the Gestures object in which save the measure
	 */
	private void storeGesture(boolean condition, Gestures.Name name, float measure, Gestures gestures) {
		if(condition)
		{
			if(measure>0 && gestures.getMeasureFromGesture(name)!=measure) {
				gestures.addMeasureToGesture(name, measure);
				System.out.println("Misurazione "+name+": "+measure);
			}

		}
		else
		{
			if(gestures.isGestureActive(name)){
				gestures.setGestureNull(name);
				System.out.println("Misurazione "+name+": "+gestures.getMeasureFromGesture(name));
			}

		}
	}



}  


