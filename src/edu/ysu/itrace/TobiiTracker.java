package edu.ysu.itrace;

import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Date;
import edu.ysu.itrace.exceptions.*;
import javax.swing.*;
import java.awt.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

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

	private static class Calibrator extends JFrame
	{
		private TobiiTracker parent = null;
		private final int CALIBRATION_POINTS = 6;
		private JLabel[] calibration_points = new JLabel[CALIBRATION_POINTS];
		private final int MILISECONDS_BETWEEN_POINTS = 2000;

		public Calibrator(TobiiTracker tracker) throws CalibrationException
		{
			parent = tracker;

			//Create calibration points
			JPanel grid = new JPanel(new GridLayout(2, 3));
			BufferedImage calibration_point = null;
			try
			{
				calibration_point = ImageIO.read(new File("res/calibration_point.png"));
			}
			catch (IOException e)
			{
				throw new CalibrationException();
			}
			for (int i = 0; i < CALIBRATION_POINTS; ++i)
			{
				calibration_points[i] = new JLabel(new ImageIcon(calibration_point));
				calibration_points[i].setVisible(false);
				grid.add(calibration_points[i]);
			}
			getContentPane().add(grid);

			GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().
				getDefaultScreenDevice();
			DisplayMode mode = device.getDisplayMode();
			setUndecorated(true);
			device.setFullScreenWindow(this);
			setAlwaysOnTop(true);
			setResizable(false);
		}

		public void calibrate() throws CalibrationException
		{
			setVisible(true);
			jniStartCalibration();
			for (int i = 0; i < CALIBRATION_POINTS; ++i)
			{
				displayCalibrationPoint(i);
				try
				{
					Thread.sleep(MILISECONDS_BETWEEN_POINTS);
				}
				catch (InterruptedException e)
				{
					jniStopCalibration();
					throw new CalibrationException();
				}
				Rectangle window_bounds = GraphicsEnvironment.
					getLocalGraphicsEnvironment().getMaximumWindowBounds();
				double x = (calibration_points[i].getLocationOnScreen().x +
					(0.5 * calibration_points[i].getWidth())) / window_bounds.width;
				double y = (calibration_points[i].getLocationOnScreen().y +
					(0.5 * calibration_points[i].getHeight())) / window_bounds.height;
				System.out.println("(" + x + ", " + y + ")");
				jniAddPoint(x, y);
			}
			if (!jniStopCalibration())
				throw new CalibrationException();
			setVisible(false);
			return;
		}

		private void displayCalibrationPoint(int i)
		{
			for (int j = 0; j < CALIBRATION_POINTS; ++j)
				calibration_points[j].setVisible(i == j);
		}

		private native void jniAddPoint(double x, double y);
		private native void jniStartCalibration();
		private native boolean jniStopCalibration();
	}

	private BackgroundThread bg_thread = null;
	private volatile ByteBuffer native_data = null;
	private LinkedBlockingQueue<Gaze> gaze_points = new LinkedBlockingQueue<Gaze>();

	static { System.loadLibrary("TobiiTracker"); }

	public TobiiTracker() throws EyeTrackerConnectException, CalibrationException
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
		//Calibrate
		Calibrator calibration = new Calibrator(null);
		calibration.calibrate();
	}

	public static void main(String[] args)
	{
		try
		{
			TobiiTracker tobii_tracker = new TobiiTracker();
			System.out.println("Connected successfully to eyetracker.");

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
		catch (CalibrationException e)
		{
			System.out.println("Could not calibrate. Try again.");
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
