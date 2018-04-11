package it.univaq.au4h.observers;

import java.util.HashMap;

import org.openni.CalibrationProgressEventArgs;
import org.openni.CalibrationProgressStatus;
import org.openni.IObservable;
import org.openni.IObserver;
import org.openni.PoseDetectionCapability;
import org.openni.SkeletonCapability;
import org.openni.SkeletonJoint;
import org.openni.SkeletonJointPosition;
import org.openni.StatusException;

import it.univaq.au4h.utility.EventsCapability;

public class CalibrationCompleteObserver implements IObserver<CalibrationProgressEventArgs>
{
	private SkeletonCapability skeletonCap;
	private PoseDetectionCapability poseDetectionCap;
	private String calibPose;
	private HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>> joints;
	private HashMap<Integer, EventsCapability> events;
	
	public CalibrationCompleteObserver(SkeletonCapability sCap, PoseDetectionCapability pCap, String cPose,HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>> j,HashMap<Integer, EventsCapability> e) {
		this.skeletonCap=sCap;
		this.poseDetectionCap=pCap;
		this.calibPose=cPose;
		this.joints=j;
		this.events=e;
	}
	
	public void update(IObservable<CalibrationProgressEventArgs> observable, CalibrationProgressEventArgs args)
	{
		System.out.println("Calibraion complete: " + args.getStatus());
		try
		{
			if (args.getStatus() == CalibrationProgressStatus.OK)
			{
				System.out.println("starting tracking "  +args.getUser());
				this.skeletonCap.startTracking(args.getUser());
				this.joints.put(new Integer(args.getUser()), new HashMap<SkeletonJoint, SkeletonJointPosition>());
				this.events.put(new Integer(args.getUser()), new EventsCapability());
			}
			else if (args.getStatus() != CalibrationProgressStatus.MANUAL_ABORT)
			{
				if (skeletonCap.needPoseForCalibration())
				{
					poseDetectionCap.startPoseDetection(calibPose, args.getUser());
				}
				else
				{
					skeletonCap.requestSkeletonCalibration(args.getUser(), true);
				}
			}
		} 
		catch (StatusException e)
		{
			e.printStackTrace();
		}
	}
}
