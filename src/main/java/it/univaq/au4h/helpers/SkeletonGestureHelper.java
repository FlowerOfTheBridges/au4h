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

import it.univaq.au4h.models.GestureName;
import it.univaq.au4h.models.Gestures;




public class SkeletonGestureHelper
{

	private HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>> userSkels;
	private HashMap<Integer, Gestures> userGestures;

	public SkeletonGestureHelper(HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>> uSkels,HashMap<Integer, Gestures> uEvCap){  
		this.userSkels = uSkels;
		this.userGestures=uEvCap;
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
		if (skel != null && gestures != null && this.allJointsActive(skel)) {
			guitarHero(skel, gestures);
			rightHandUp(skel, gestures);
			openLegs(skel, gestures);
			neckCamera(skel, gestures);
		}
		return this.userGestures.get(userID);
	} 

	private float distApart3D(Point3D p1, Point3D p2){
		
		float dist = (float) Math.sqrt( 
				(p1.getX() - p2.getX())*(p1.getX() - p2.getX()) +
				(p1.getY() - p2.getY())*(p1.getY() - p2.getY()));
		return dist;
	}  

	/**
	 * 
	 * @param p1
	 * @param p2
	 * @param p3
	 * @return 
	 */
	private float angleSkelJoints3D(Point3D p1, Point3D p2, Point3D p3){
		javafx.geometry.Point3D vertex = new javafx.geometry.Point3D(p1.getX(), p1.getY(), p1.getZ());
		javafx.geometry.Point3D aux1 = new javafx.geometry.Point3D(p2.getX(), p2.getY(), p2.getZ());
		javafx.geometry.Point3D aux2 = new javafx.geometry.Point3D(p3.getX(), p3.getY(), p3.getZ());

		return (float) vertex.angle(aux1, aux2);
		
	}



	private void neckCamera(HashMap<SkeletonJoint, SkeletonJointPosition> skel, Gestures gestures){
		Point3D leftHand=getJointPos(skel, SkeletonJoint.LEFT_HAND);
		Point3D leftShoulder=getJointPos(skel,SkeletonJoint.LEFT_SHOULDER);
		Point3D rightHand = getJointPos(skel, SkeletonJoint.RIGHT_HAND);
		if(leftHand!=null && leftShoulder!=null && rightHand!=null){
			float angle=this.angleSkelJoints3D(leftShoulder, new Point3D(leftHand.getX(),leftHand.getY(),leftShoulder.getZ()), leftHand);
			boolean condition=leftHand.getZ()<rightHand.getZ()-(rightHand.getZ())/10;
			this.storeGesture(condition,GestureName.NECK_CAMERA,angle,gestures);
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
		Point3D torso=getJointPos(skel, SkeletonJoint.TORSO);
		Point3D shoulderRPt = getJointPos(skel, SkeletonJoint.RIGHT_SHOULDER);
		Point3D shoulderLPt = getJointPos(skel, SkeletonJoint.LEFT_SHOULDER);

		if((lkPt!=null) && (rkPt!=null) && torso!=null && shoulderRPt!=null  && shoulderLPt!=null) {
			float degree=angleSkelJoints3D(torso,lkPt,rkPt);
			boolean condition=distApart3D(lkPt,rkPt)>distApart3D(shoulderRPt, shoulderLPt);
			this.storeGesture(condition, GestureName.OPEN_LEGS, degree, gestures);
		}
		
	}


	private void guitarHero(HashMap<SkeletonJoint, SkeletonJointPosition> skel, Gestures gestures) {
		Point3D lhandPt=getJointPos(skel,SkeletonJoint.LEFT_HAND);
		Point3D lshoulderPt = getJointPos(skel, SkeletonJoint.NECK);
		Point3D lhPt=getJointPos(skel, SkeletonJoint.LEFT_HIP);
		Point3D rhPt=getJointPos(skel, SkeletonJoint.RIGHT_HIP);

		if ((lhandPt!= null) && (lshoulderPt!=null) && (lhPt!=null) && (rhPt!=null)) {
			float degree=angleSkelJoints3D(lhPt,lhandPt,new Point3D(lhandPt.getX(),lhPt.getY(),lhPt.getZ()));
			boolean condition=lhandPt.getY() < lshoulderPt.getY();
			this.storeGesture(condition, GestureName.LEAN_BACK, degree, gestures);
		}
	}

	private void rightHandUp(HashMap<SkeletonJoint, SkeletonJointPosition> skel, Gestures gestures){
		Point3D rightHandPt = getJointPos(skel, SkeletonJoint.RIGHT_HAND);
		Point3D headPt = getJointPos(skel, SkeletonJoint.HEAD);
		if ((rightHandPt != null) || (headPt != null))
		{
			boolean condition=rightHandPt.getY() <= headPt.getY();
			this.storeGesture(condition, GestureName.RH_UP, 1f, gestures);
			
		}
	}  

	private Point3D getJointPos(HashMap<SkeletonJoint, SkeletonJointPosition> skel, 
			SkeletonJoint j)
	{
		SkeletonJointPosition pos = skel.get(j);
		if (pos == null)
			return null;

		if (pos.getConfidence() <= 0.5)
			return null;

		return pos.getPosition();
	}  
	
	private boolean allJointsActive(HashMap<SkeletonJoint, SkeletonJointPosition> skel) {
		for(SkeletonJoint j : skel.keySet()) {
			SkeletonJointPosition pos = skel.get(j);
			if (pos == null) {
				return false;
			}
			if (pos.getConfidence() <= 0.5) {
				return false;
			}
		}
			
		return true;	
	}

	/**
	 * Associates a measure to its gesture.
	 * @param condition the result of the condition to be verified.
	 * @param gn the gesture's name
	 * @param measure the measure to be stored
	 * @param gestures the Gestures object in which save the measure
	 */
	private void storeGesture(boolean condition, GestureName gn, float measure, Gestures gestures) {
		if(condition)
		{
			if(measure>0 && gestures.getMeasureFromEvent(gn)!=measure) {
				gestures.addMeasureToEvent(gn, measure);
				System.out.println("Misurazione "+gn+": "+measure);
			}
			
		}
		else
		{
			if(gestures.isEventActive(gn)){
				gestures.setEventNull(gn);
				System.out.println("Misurazione "+gn+": "+gestures.getMeasureFromEvent(gn));
			}
			
		}
	}
	
	
	
}  


