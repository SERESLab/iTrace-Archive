package edu.ysu.itrace;

import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.graphics.Point;
import org.osgi.framework.Bundle;

import edu.ysu.itrace.exceptions.CalibrationException;
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

	private static class Calibrator extends JFrame
	{
		private TobiiTracker parent = null;
		private final int CALIBRATION_POINTS = 9;
		private JLabel[] calibration_points = new JLabel[CALIBRATION_POINTS];
		private final int MILISECONDS_BETWEEN_POINTS = 2000;

		public Calibrator(TobiiTracker tracker) throws CalibrationException
		{
			parent = tracker;

			//Create calibration points
			JPanel grid = new JPanel(new GridLayout(3, 3));
			BufferedImage calibration_point = null;
			try
			{
				calibration_point =
					TobiiTracker.getBufferedImage("calibration_point.png");
			}
			catch (IOException | URISyntaxException e)
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
			try
			{
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
						throw new CalibrationException("Thread.sleep interrupted.");
					}
					Dimension window_bounds = Toolkit.getDefaultToolkit().getScreenSize();
					double x = (calibration_points[i].getLocationOnScreen().x +
						(0.5 * calibration_points[i].getWidth())) / window_bounds.width;
					double y = (calibration_points[i].getLocationOnScreen().y +
						(0.5 * calibration_points[i].getHeight())) / window_bounds.height;
					jniAddPoint(x, y);
				}
				jniStopCalibration();
			}
			//Rethrow CalibrationExceptions.
			catch (CalibrationException e)
			{
				throw e;
			}
			//All JNI exceptions converted to Calibration exceptions.
			catch (Exception e)
			{
				throw new CalibrationException(e.getMessage());
			}
			finally
			{
				setVisible(false);
			}
		}

		private void displayCalibrationPoint(int i)
		{
			for (int j = 0; j < CALIBRATION_POINTS; ++j)
				calibration_points[j].setVisible(i == j);
		}

		private native void jniAddPoint(double x, double y) throws RuntimeException,
			IOException;
		private native void jniStartCalibration() throws RuntimeException,
			IOException;
		private native void jniStopCalibration() throws RuntimeException,
			IOException;
	}

	private BackgroundThread bg_thread = null;
	private volatile ByteBuffer native_data = null;
	private LinkedBlockingQueue<Gaze> gaze_points =
		new LinkedBlockingQueue<Gaze>();
	private boolean display_crosshair = false;
	private JWindow debug_display_icon = new JWindow()
		{
			private Point centre = null;

			{
				try
				{
					BufferedImage crosshair =
						TobiiTracker.getBufferedImage("crosshair.png");
					add(new JLabel(new ImageIcon(crosshair)));
					centre = new Point((int) (crosshair.getWidth() / 2),
						(int) (crosshair.getHeight() / 2));
					pack();
					setAlwaysOnTop(true);
					setVisible(display_crosshair);
				}
				catch (IOException | URISyntaxException e)
				{
					System.out.println("Failed to load crosshair icon.");
				}
			}

			public void setLocation(int x, int y)
			{
				super.setLocation(x - centre.x, y - centre.y);
			}
		};

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
	}

	public static void main(String[] args)
	{
		TobiiTracker tobii_tracker = null;
		try
		{
			tobii_tracker = new TobiiTracker();
			System.out.println("Connected successfully to eyetracker.");

			Dimension window_bounds = Toolkit.getDefaultToolkit().getScreenSize();
			System.out.println("Screen size: (" + window_bounds.width + ", " +
				window_bounds.height + ")");
			try
			{
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			tobii_tracker.calibrate();

			tobii_tracker.startTracking();
			tobii_tracker.displayCrosshair(true);
			long start = (new Date()).getTime();
			while ((new Date()).getTime() < start + 5000);
			tobii_tracker.stopTracking();
			tobii_tracker.displayCrosshair(false);
			tobii_tracker.clear();

			tobii_tracker.startTracking();
			start = (new Date()).getTime();
			while ((new Date()).getTime() < start + 25000)
			{
				Gaze gaze = tobii_tracker.getGaze();
				if (gaze != null)
				{
					System.out.println("Gaze at " + gaze.getTimeStamp() + ": (" +
						(int) (gaze.getX() * window_bounds.width) + ", " +
						(int) (gaze.getY() * window_bounds.height) +
						") with validity (Left: " + gaze.getLeftValidity() + ", Right: " +
						gaze.getRightValidity() + ")");
				}
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
			tobii_tracker.close();
			System.out.println("Could not calibrate. Try again.");
		}
		catch (IOException e)
		{
			tobii_tracker.close();
			System.out.println("IO failure occurred.");
		}
		System.out.println("Done!");
	}

	public void clear()
	{
		gaze_points = new LinkedBlockingQueue<Gaze>();
	}

	public void calibrate() throws CalibrationException
	{
		debug_display_icon.setVisible(false);
		Calibrator calibration = new Calibrator(this);
		calibration.calibrate();
		if (display_crosshair)
			debug_display_icon.setVisible(true);
	}

	public Gaze getGaze()
	{
		return gaze_points.poll();
	}

	public void newGazePoint(long timestamp, double left_x, double left_y,
		double right_x, double right_y, int left_validity, int right_validity)
	{
		//Average left and right eyes for each value.
		double x = (left_x + right_x) / 2;
		double y = (left_y + right_y) / 2;

		//Clamp values to [0.0, 1.0].
		if (x >= 1.0)
			x = 1.0;
		else if (x <= 0.0)
			x = 0.0;
		if (y >= 1.0)
			y = 1.0;
		else if (y <= 0.0)
			y = 0.0;

		double gaze_left_validity = 1.0 - ((double) left_validity / 4.0);
		double gaze_right_validity = 1.0 - ((double) right_validity / 4.0);

		if (display_crosshair)
		{
			Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
			int screen_x = (int) (screen_size.width * x);
			int screen_y = (int) (screen_size.height * y);
			debug_display_icon.setLocation(screen_x, screen_y);
		}

		try
		{
			Gaze gaze = new Gaze(x, y, gaze_left_validity, gaze_right_validity,
				new Date(timestamp / 1000));
			gaze_points.put(gaze);
		}
		catch (InterruptedException e)
		{
			//Ignore this point.
		}
	}

	private native boolean jniConnectTobiiTracker(int timeout_seconds);
	public native void close();
	public native void startTracking() throws RuntimeException, IOException;
	public native void stopTracking() throws RuntimeException, IOException;

	public void displayCrosshair(boolean enabled)
	{
		display_crosshair = enabled;
		debug_display_icon.setVisible(enabled);
	}

	private static BufferedImage getBufferedImage(String resource_name)
		throws IOException, URISyntaxException
	{
		BufferedImage result = null;
		Bundle bundle = Platform.getBundle("edu.ysu.itrace");
		//Eclipse
		if (bundle != null)
		{
			URL file_url = bundle.getEntry("res/" + resource_name);
			result = ImageIO.read(new File(FileLocator.resolve(file_url).toURI()));
		}
		//No eclipse
		else
		{
			result = ImageIO.read(new File("res/" + resource_name));
		}
		return result;
	}
}
