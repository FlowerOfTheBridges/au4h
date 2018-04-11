package it.univaq.au4h.observers;

import org.openni.IObservable;
import org.openni.IObserver;
import org.openni.PoseDetectionCapability;
import org.openni.SkeletonCapability;
import org.openni.StatusException;
import org.openni.UserEventArgs;

public class NewUserObserver implements IObserver<UserEventArgs>
{
	private SkeletonCapability skeletonCap;
	private PoseDetectionCapability poseDetectionCap;
	private String calibPose;
	
	public NewUserObserver(SkeletonCapability sCap, PoseDetectionCapability pCap, String cPose) {
		this.skeletonCap=sCap;
		this.poseDetectionCap=pCap;
		this.calibPose=cPose;
	}
	
	public void update(IObservable<UserEventArgs> observable, UserEventArgs args)
	{
		System.out.println("New user " + args.getId());
		try
		{
			if (skeletonCap.needPoseForCalibration())
			{
				poseDetectionCap.startPoseDetection(calibPose, args.getId());
			}
			else
			{   
				skeletonCap.requestSkeletonCalibration(args.getId(), true);
			}

		} 
		catch (StatusException e)
		{
			e.printStackTrace();
		}
	}
}
