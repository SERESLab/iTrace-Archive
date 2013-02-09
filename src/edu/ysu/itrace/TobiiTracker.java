package edu.ysu.itrace;

import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Date;
import edu.ysu.itrace.exceptions.EyeTrackerConnectException;

public class TobiiTracker implements IEyeTracker
{
	private static class BackgroundThread extends Thread
	{
		private TobiiTracker parent = null;

		public BackgroundThread(TobiiTracker parent)
		{
			this.parent = parent;
		}

		public void run()
		{
			//TODO: Handle error condition
			jniBeginTobiiMainloop();
		}

		private native boolean jniBeginTobiiMainloop();
	}

	private static class Calibrator
	{
		private TobiiTracker parent = null;

		public Calibrator(TobiiTracker tracker)
		{
			parent = tracker;

			//Test calibration
			try
			{
				while (true)
				{
					jniStartCalibration();
					System.out.println("View top-left point");
					Thread.sleep(1000);
					jniAddPoint(0.25, 0.25);
					System.out.println("View top-centre point");
					Thread.sleep(1000);
					jniAddPoint(0.50, 0.25);
					System.out.println("View top-right point");
					Thread.sleep(1000);
					jniAddPoint(0.75, 0.25);
					System.out.println("View bottom-left point");
					Thread.sleep(1000);
					jniAddPoint(0.25, 0.75);
					System.out.println("View bottom-centre point");
					Thread.sleep(1000);
					jniAddPoint(0.50, 0.75);
					System.out.println("View bottom-right point");
					Thread.sleep(1000);
					jniAddPoint(0.75, 0.75);
					if (jniStopCalibration())
						break;
				}
			}
			catch (InterruptedException e)
			{
			}
		}

		private native void jniAddPoint(double x, double y);
		private native void jniStartCalibration();
		private native boolean jniStopCalibration();
	}

	private BackgroundThread bg_thread = null;
	private volatile ByteBuffer native_data = null;
	private LinkedBlockingQueue<Gaze> gaze_points = new LinkedBlockingQueue<Gaze>();

	static { System.loadLibrary("TobiiTracker"); }

	public TobiiTracker() throws EyeTrackerConnectException
	{
		//Initialise the background thread which functions as the main loop in the
		//Tobii SDK.
		bg_thread = new BackgroundThread(this);
		bg_thread.start();
		while (native_data == null); //Wait until background thread sets native_data
		if (!jniConnectTobiiTracker(10))
		{
			this.close();
			throw new EyeTrackerConnectException();
		}
	}

	public static void main(String[] args)
	{
		try
		{
			TobiiTracker tobii_tracker = new TobiiTracker();
			System.out.println("Connected successfully to eyetracker.");

			Calibrator calibration = new Calibrator(tobii_tracker);

			tobii_tracker.startTracking();
			long start = (new Date()).getTime();
			while ((new Date()).getTime() < start + 25000)
			{
				Gaze gaze = tobii_tracker.getGaze();
				System.out.println("Gaze at " + gaze.getTimeStamp() + ": (" +
					gaze.getX() + ", " + gaze.getY() + ")");
			}
			tobii_tracker.stopTracking();

			tobii_tracker.close();
		}
		catch (EyeTrackerConnectException e)
		{
			System.out.println("Failed to connect to Tobii eyetracker.");
		}
		System.out.println("Done!");
	}

	public Gaze getGaze()
	{
		try
		{
			return gaze_points.take();
		}
		catch (InterruptedException e)
		{
			return null;
		}
	}

	public void newGazePoint(long timestamp, double left_x, double left_y,
		double right_x, double right_y)
	{
		//Average left and right eyes for each value.
		double x = (left_x + right_x) / 2;
		double y = (left_y + right_y) / 2;

		try
		{
			gaze_points.put(new Gaze(x, y, new Date(timestamp / 1000)));
		}
		catch (InterruptedException e)
		{
			//Ignore this point.
		}
	}

	private native boolean jniConnectTobiiTracker(int timeout_seconds);
	public native boolean close();
	public native boolean startTracking();
	public native boolean stopTracking();
}
