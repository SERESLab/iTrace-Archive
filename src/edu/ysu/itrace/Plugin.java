package edu.ysu.itrace;

import java.util.ArrayList;

import org.eclipse.swt.widgets.MessageBox;

public class Plugin {
	IEyeTracker tracker;
	GazeRepository gazeRepository;
	AoiRepository aoiRepository;
	LinkFinder linkFinder;
	
	public Plugin(){
		
	}
	
	private void listTrackers(){
		ArrayList<IEyeTracker> trackers = EyeTrackerFactory.getAvailableEyeTrackers();
	}
	
	private void selectTracker(int index) {
		tracker = EyeTrackerFactory.getConcreteEyeTracker(index);
	}
	
	private void startTracking(){
		if(tracker != null) {
			
		}
	}
	
	private void stopTracking(){
		if(tracker != null) {
			
		}
	}
}
