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
import it.univaq.au4h.utility.EventsCapability;
import it.univaq.au4h.utility.GestureName;




public class SkeletonGestureHelper
{

	private HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>> userSkels;
	private HashMap<Integer, EventsCapability> usersEvCap;

	public SkeletonGestureHelper(HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>> uSkels,HashMap<Integer, EventsCapability> uEvCap){  
		this.userSkels = uSkels;
		this.usersEvCap=uEvCap;
	} 

	/**
	 * check which gestures have just started or just finished, and
     * eventually add their measure to the GesturesCapability.
	 * @param userID the user to analyze
	 */
	public EventsCapability checkGests(int userID){
		HashMap<SkeletonJoint, SkeletonJointPosition> skel = userSkels.get(userID);
		EventsCapability evCap=usersEvCap.get(userID);
		if (skel != null && evCap != null) {
			guitarHero(userID, skel, evCap);
			rightHandUp(userID, skel, evCap);
			openLegs(userID, skel, evCap);
			neckCamera(userID,skel, evCap);
		}
		return this.usersEvCap.get(userID);
	} 

	private float distApart2D(Point3D p1, Point3D p2){
		float dist = (float) Math.sqrt( 
				(p1.getX() - p2.getX())*(p1.getX() - p2.getX()) +
				(p1.getY() - p2.getY())*(p1.getY() - p2.getY())); 
		return dist;
	}  

	private float distApart3D(Point3D p1, Point3D p2){
		
		float dist = (float) Math.sqrt( 
				(p1.getX() - p2.getX())*(p1.getX() - p2.getX()) +
				(p1.getY() - p2.getY())*(p1.getY() - p2.getY())+
				(p1.getZ() - p2.getZ())*(p1.getZ() - p2.getZ()) );
		return dist;
	}  

	/**
	 * 
	 * @param p1
	 * @param p2
	 * @param p3
	 * @return 
	 */
	private int angleSkelJoints3D(Point3D p1, Point3D p2, Point3D p3){
		javafx.geometry.Point3D vertex = new javafx.geometry.Point3D(p1.getX(), p1.getY(), p1.getZ());
		javafx.geometry.Point3D aux1 = new javafx.geometry.Point3D(p2.getX(), p2.getY(), p2.getZ());
		javafx.geometry.Point3D aux2 = new javafx.geometry.Point3D(p3.getX(), p3.getY(), p3.getZ());

		double angle = vertex.angle(aux1, aux2);
		return (int) angle;
	}



	private void neckCamera(int userID, HashMap<SkeletonJoint, SkeletonJointPosition> skel, EventsCapability evCap){
		Point3D leftHand=getJointPos(skel, SkeletonJoint.LEFT_HAND);
		Point3D leftShoulder=getJointPos(skel,SkeletonJoint.LEFT_SHOULDER);
		Point3D rightHand = getJointPos(skel, SkeletonJoint.RIGHT_HAND);
		if(leftHand!=null && leftShoulder!=null && rightHand!=null){
			int angle=this.angleSkelJoints3D(leftShoulder, new Point3D(leftHand.getX(),leftHand.getY(),leftShoulder.getZ()), leftHand);
			this.storeEventCapability((leftHand.getZ()<rightHand.getZ()-(rightHand.getZ()/10)),GestureName.NECK_CAMERA,angle,evCap);
		}	
	}

	private void openLegs(int userID, HashMap<SkeletonJoint, SkeletonJointPosition> skel, EventsCapability evCap){
		Point3D lkPt=getJointPos(skel, SkeletonJoint.LEFT_KNEE);
		Point3D rkPt=getJointPos(skel, SkeletonJoint.RIGHT_KNEE);
		Point3D lhPt=getJointPos(skel, SkeletonJoint.LEFT_HIP);
		Point3D rhPt=getJointPos(skel, SkeletonJoint.RIGHT_HIP);
		Point3D torso=getJointPos(skel, SkeletonJoint.TORSO);
		Point3D shoulderRPt = getJointPos(skel, SkeletonJoint.RIGHT_SHOULDER);
		Point3D shoulderLPt = getJointPos(skel, SkeletonJoint.LEFT_SHOULDER);

		if((lkPt!=null) && (rkPt!=null) && (lhPt!=null) && (rhPt!=null) && torso!=null && shoulderRPt!=null  && shoulderLPt!=null) {
			Point3D middle=new Point3D((lhPt.getX()+rhPt.getX())/2,(lhPt.getY()+rhPt.getY())/2,(lhPt.getZ()+rhPt.getZ())/2);
			int degree=angleSkelJoints3D(middle,lkPt,rkPt);
			this.storeEventCapability((distApart3D(lkPt,rkPt)>distApart3D(shoulderRPt, shoulderLPt)), GestureName.OPEN_LEGS, degree, evCap);
		}
		
	}


	private void guitarHero(int userID, HashMap<SkeletonJoint, SkeletonJointPosition> skel, EventsCapability evCap) {
		Point3D lhandPt=getJointPos(skel,SkeletonJoint.LEFT_HAND);
		Point3D lshoulderPt = getJointPos(skel, SkeletonJoint.NECK);
		Point3D lhPt=getJointPos(skel, SkeletonJoint.LEFT_HIP);
		Point3D rhPt=getJointPos(skel, SkeletonJoint.RIGHT_HIP);

		if ((lhandPt!= null) && (lshoulderPt!=null) && (lhPt!=null) && (rhPt!=null)) {
			int degree=angleSkelJoints3D(lhPt,lhandPt,new Point3D(lhandPt.getX(),lhPt.getY(),lhPt.getZ()));
			this.storeEventCapability(lhandPt.getY() < lshoulderPt.getY(), GestureName.LEAN_BACK, degree, evCap);
		}
	}

	private void rightHandUp(int userID, HashMap<SkeletonJoint, SkeletonJointPosition> skel, EventsCapability evCap){
		Point3D rightHandPt = getJointPos(skel, SkeletonJoint.RIGHT_HAND);
		Point3D headPt = getJointPos(skel, SkeletonJoint.HEAD);
		if ((rightHandPt == null) || (headPt == null))
			return;

		if (rightHandPt.getY() <= headPt.getY()) {  
			if (!evCap.isEventActive(GestureName.RH_UP)) {
				evCap.addMeasureToEvent(GestureName.RH_UP, 1f);
				System.out.println("Hand up! Lets rock");
			}
			else{
				evCap.setEventNull(GestureName.RH_UP);
				System.out.println("Hand up! Keep calm...");
			}
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


	private void storeEventCapability(boolean condition, GestureName gn, float measure, EventsCapability evCap) {
		if(condition)
		{
			evCap.addMeasureToEvent(gn, measure);
			System.out.println("Misurazione "+gn+": "+measure);
		}
		else
		{
			if(evCap.isEventActive(gn)){
				evCap.setEventNull(gn);
				System.out.println("Misurazione "+gn+": OFF");
			}
			
		}
	}
}  


