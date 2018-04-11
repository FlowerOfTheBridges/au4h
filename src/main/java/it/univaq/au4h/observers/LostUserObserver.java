package it.univaq.au4h.observers;

import java.util.HashMap;

import org.openni.IObservable;
import org.openni.IObserver;
import org.openni.SkeletonJoint;
import org.openni.SkeletonJointPosition;
import org.openni.UserEventArgs;

import it.univaq.au4h.utility.EventsCapability;

public class LostUserObserver implements IObserver<UserEventArgs>
{
	private HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>> joints;
	private HashMap<Integer, EventsCapability> events;
	
	public LostUserObserver(HashMap<Integer, HashMap<SkeletonJoint, SkeletonJointPosition>> j, HashMap<Integer, EventsCapability> e) {
		this.joints=j;
		this.events=e;
	}
	
	public void update(IObservable<UserEventArgs> observable, UserEventArgs args)
	{
		System.out.println("Lost user " + args.getId());
		this.joints.remove(args.getId());
		this.events.remove(args.getId());
	}
}