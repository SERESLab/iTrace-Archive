package edu.ysu.itrace.trackers;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

import edu.ysu.itrace.Gaze;
import edu.ysu.itrace.IEyeTracker;
import edu.ysu.itrace.exceptions.CalibrationException;

/**
 * Provides an interface for the EyeX eye tracker.
 *
 */
public class EyeXTracker implements IEyeTracker {

	private LinkedBlockingQueue<Gaze> gaze_points = new LinkedBlockingQueue<Gaze>();
    private LinkedBlockingQueue<Gaze> recentGazes = new LinkedBlockingQueue<Gaze>();
    private double xDrift = 0;
    private double yDrift = 0;
	
	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void clear() {
		gaze_points = new LinkedBlockingQueue<Gaze>();
	}

	@Override
	public void calibrate() throws CalibrationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startTracking() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stopTracking() throws IOException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Gaze getGaze() {
		 return gaze_points.poll();
	}

	@Override
	public void displayCrosshair(boolean enabled) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setXDrift(int drift) {
		 xDrift = ((double) drift) / 100;
	}

	@Override
	public void setYDrift(int drift) {
		 yDrift = ((double) drift) / 100;
	}

}