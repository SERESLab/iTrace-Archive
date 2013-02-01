package edu.ysu.itrace;

import java.util.ArrayList;

public class EyeTrackerFactory {
	
	public static ArrayList<IEyeTracker> getAvailableEyeTrackers(){
		//TODO read a config file and use reflection to
		//  build each of the trackers in the arraylist
		return null;
	}
	
	public static IEyeTracker getConcreteEyeTracker(int i) {
		return new IEyeTracker(){

			@Override
			public Gaze getGaze() {
				// TODO Auto-generated method stub
				return null;
			}};
		//return getAvailableEyeTrackers().get(i);
	}
}
