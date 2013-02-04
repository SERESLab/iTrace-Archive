package edu.ysu.itrace;

import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

public class TobiiTracker implements IEyeTracker
{
	private class BackgroundThread extends Thread
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

	private int screen_width = 0, screen_height = 0;
	private BackgroundThread bg_thread = null;
	private ByteBuffer native_data = null;
	private LinkedBlockingQueue<Gaze> gaze_points = new LinkedBlockingQueue<Gaze>();

	static { System.loadLibrary("TobiiTracker"); }

	public TobiiTracker(int screen_width, int screen_height)
	{
		//Initialise the background thread which functions as the main loop in the
		//Tobii SDK.
		bg_thread = new BackgroundThread(this);
		bg_thread.run();
		while (native_data == null); //Wait until background thread sets native_data
		jniConnectTobiiTracker(10);
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

	private void newGazePoint(long timestamp, double left_x, double left_y,
		double right_x, double right_y)
	{
		//Average left and right eyes for each value.
		int x = (int) Math.round(((left_x + right_x) / 2) * screen_width);
		int y = (int) Math.round(((left_y + right_y) / 2) * screen_height);

		try
		{
			gaze_points.put(new Gaze(x, y, 0));
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
