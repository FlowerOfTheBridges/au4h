package it.univaq.au4h.observers;

import java.util.HashMap;

import org.openni.IObservable;
import org.openni.IObserver;
import org.openni.SkeletonJoint;
import org.openni.SkeletonJointPosition;
import org.openni.UserEventArgs;

import it.univaq.au4h.models.Gestures;

public class LostUserObserver implements IObserver<UserEventArgs>
{
	private HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>> joints;
	private HashMap<Integer, Gestures> gestures;
	
	public LostUserObserver(HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>> j, HashMap<Integer, Gestures> gest) {
		this.joints=j;
		this.gestures=gest;
	}
	
	public void update(IObservable<UserEventArgs> observable, UserEventArgs args)
	{
		System.out.println("Lost user " + args.getId());
		this.joints.remove(args.getId());
		this.gestures.remove(args.getId());
	}
}