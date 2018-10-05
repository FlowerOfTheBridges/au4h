package it.univaq.au4h.observers;

import org.openni.IObservable;
import org.openni.IObserver;
import org.openni.PoseDetectionCapability;
import org.openni.PoseDetectionEventArgs;
import org.openni.SkeletonCapability;
import org.openni.StatusException;

public class PoseDetectedObserver implements IObserver<PoseDetectionEventArgs>{
	
	private SkeletonCapability skeletonCap;
	private PoseDetectionCapability poseDetectionCap;
	
	public PoseDetectedObserver(SkeletonCapability sCap, PoseDetectionCapability pCap) {
		this.skeletonCap=sCap;
		this.poseDetectionCap=pCap;
	}
	
	public void update(IObservable<PoseDetectionEventArgs> observable,PoseDetectionEventArgs args)
	{
		System.out.println("Pose " + args.getPose() + " detected for " + args.getUser());
		try
		{
			poseDetectionCap.stopPoseDetection(args.getUser());
			skeletonCap.requestSkeletonCalibration(args.getUser(), true);
		} 
		catch (StatusException e)
		{
			e.printStackTrace();
		}
	}

}
