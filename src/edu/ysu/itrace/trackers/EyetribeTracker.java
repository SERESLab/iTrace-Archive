package edu.ysu.itrace.trackers;

import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.jface.dialogs.MessageDialog;

import com.theeyetribe.client.GazeManager;
import com.theeyetribe.client.GazeManager.ApiVersion;
import com.theeyetribe.client.GazeManager.ClientMode;
import com.theeyetribe.client.ICalibrationProcessHandler;
import com.theeyetribe.client.IGazeListener;
import com.theeyetribe.client.ITrackerStateListener;
import com.theeyetribe.client.data.CalibrationResult;
import com.theeyetribe.client.data.CalibrationResult.CalibrationPoint;
import com.theeyetribe.client.data.GazeData;

import edu.ysu.itrace.Activator;
import edu.ysu.itrace.Gaze;
import edu.ysu.itrace.IEyeTracker;
import edu.ysu.itrace.exceptions.CalibrationException;

/**
 * Provides an interface for the EyeTribe eye tracker.
 *
 */
public class EyetribeTracker implements IEyeTracker {

	/**
	 * Path to the eye tribe server.
	 */
	private static final String SERVER_PATH = "C:\\Program Files (x86)\\EyeTribe\\Server\\EyeTribe.exe";
	
	private GazeManager gm = GazeManager.getInstance();
	private LinkedBlockingQueue<Gaze> gaze_points = new LinkedBlockingQueue<Gaze>();
	private LinkedBlockingQueue<Gaze> recentGazes = new LinkedBlockingQueue<Gaze>();
    private Calibrator calibrator;
    private double xDrift = 0;
    private double yDrift = 0;
    
    /**
     * Listener for new gaze events. Called from the EyeTribe library.
     * 
     */
	private static class GazeListener implements IGazeListener {

		private EyetribeTracker tracker;
		
		public GazeListener(EyetribeTracker tracker) {
			this.tracker = tracker;
		}
		
		/**
		 * Called every time a new gaze is measured by the eye tracker
		 */
		@Override
		public void onGazeUpdate(GazeData gazeData) {
			tracker.newGazePoint(gazeData.timeStamp, gazeData.leftEye.smoothedCoordinates.x, gazeData.leftEye.smoothedCoordinates.y,
					gazeData.rightEye.smoothedCoordinates.x, gazeData.rightEye.smoothedCoordinates.y);
		}
		
	}
	
	/**
	 * Listener for new status events from the eye tracker. Called from the EyeTribe library.
	 *
	 */
	private static class StateListener implements ITrackerStateListener {

		@Override
		public void OnScreenStatesChanged(int screenIndex, int screenResolutionWidth, int screenResolutionHeight,
				float screenPhysicalWidth, float screenPhysicalHeight) {
			// Do nothing
		}

		@Override
		public void onTrackerStateChanged(int trackerState) {
			switch(trackerState) {
				case 0:
					System.out.println("Connected");
				break;
				case 1:
					System.out.println("Not connected");
				break;
				case 2:
					System.out.println("Firewall");
				case 3:
					System.out.println("Tracker is connected to USB3 port");
				break;
				case 4:
					System.out.println("No stream received.");
				break;
				default:
					System.out.println("Unknown");
				break;
			}
		}
		
	}
	
	/**
	 * Listener for calibration events. Called from the EyeTribe Library.
	 *
	 */
	private static class CalibrationListener implements ICalibrationProcessHandler {

		@Override
		public void onCalibrationProcessing() {
			// Do nothing.
		}

		@Override
		public void onCalibrationProgress(double progress) {
			// Do nothing.
		}

		/**
		 * Returns the result of the calibration
		 */
		@Override
		public void onCalibrationResult(CalibrationResult result) {
			System.out.println("Calibration result: " + result.result);
			System.out.println("Failed points:");
			for (CalibrationPoint point : result.calibpoints) {
				if (point.state != 2) {
					System.out.println(point.coordinates);
				}
			}
		}

		@Override
		public void onCalibrationStarted() {
			// Do nothing
		}
		
	}
	
	@SuppressWarnings("serial")
	private static class Calibrator extends edu.ysu.itrace.Calibrator {
	
		private GazeManager gm;
		
		public Calibrator(GazeManager gm) throws IOException {
			super();
			this.gm = gm;
		}

		@Override
		protected void startCalibration() throws Exception {
			final ICalibrationProcessHandler calibrationListener = new CalibrationListener();
			//Tell the eye tracker how many calibration points are used for the calibration
			gm.calibrationStart(Calibrator.CALIBRATION_POINTS, calibrationListener);
		}

		@Override
		protected void stopCalibration() throws Exception {
			//Do nothing
		}

		@Override
		protected void useCalibrationPoint(double x, double y, double absoluteX, double absoluteY) throws Exception {
			//Let the eye settle for 300 ms
			Thread.sleep(300);
			//Inform the eye tracker about the calibration point that is used
			gm.calibrationPointStart((int) absoluteX, (int) absoluteY);
		}
		
		protected void usedCalibrationPoint(double x, double y, double absoluteX, double absoluteY) throws Exception {
			//Inform the eye tracker about the calibration point that was used
			gm.calibrationPointEnd();
		}
		
	}
	
	@Override
	public void close() {
		// Do nothing
	}

	@Override
	public void clear() {
		gaze_points = new LinkedBlockingQueue<Gaze>();
	}
	
	private void startServer() throws IOException {
		File file = new File(SERVER_PATH);
		if (! file.exists()) {
			throw new IllegalArgumentException("The file " + SERVER_PATH + " does not exist");
		}
		Runtime.getRuntime().exec(file.getAbsolutePath());
	}
	/**
	 * Establish a connection to the eye tracker
	 */
	private void activateTracker() {
		try {
			startServer();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		boolean success = gm.activate(ApiVersion.VERSION_1_0, ClientMode.PUSH);
		if (success) {
			System.out.println("Sucessfully activated!");	
		} else {
			System.out.println("Wasn't able to establish a connection");
		}
	}

	/**
	 * Starts the calibration phase
	 */
	@Override
	public void calibrate() throws CalibrationException {
		if (!gm.isActivated()) {
			activateTracker();
		}
		
		while (!gm.isActivated()) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		try {
			calibrator = new Calibrator(gm);
			calibrator.calibrate();
			
		} catch (IOException e) {
			throw new CalibrationException(e.getMessage());
		}
	}

	/**
	 * Start recording gaze points
	 */
	@Override
	public void startTracking() throws IOException {
		final GazeListener gazeListener = new GazeListener(this);
		final ITrackerStateListener stateListener = new StateListener();
		
		gm.addGazeListener(gazeListener);
		gm.addTrackerStateListener(stateListener);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			
			@Override
			public void run() {
				gm.removeGazeListener(gazeListener);
				gm.removeTrackerStateListener(stateListener);
			}
			
		});
		
		if (!gm.isActivated()) {
			activateTracker();
		}
		
		if (!gm.isCalibrated()) {
			Activator.getDefault().getWorkbench().getDisplay().asyncExec(new Runnable() {
				
				@Override
				public void run() {
					MessageDialog.openWarning(Activator.getDefault().getWorkbench().getDisplay().getActiveShell(),
							"Error!", "The eye tracker has to be calibrated first!");
				}
			});
		}
		
	}

	/**
	 * Stop recording gaze points
	 */
	@Override
	public void stopTracking() throws IOException {
		//TODO: Stop tracking
	}

	@Override
	public Gaze getGaze() {
		return gaze_points.poll();
	}

	@Override
	public void displayCrosshair(boolean enabled) {
		calibrator.displayCrosshair(enabled);
	}

	@Override
	public void setXDrift(int drift) {
		xDrift = ((double) drift) / 100;
	}

	@Override
	public void setYDrift(int drift) {
		yDrift = ((double) drift) / 100;
	}
	
	public void newGazePoint(long timestamp, double left_x, double left_y,
		double right_x, double right_y) {
	   
	 	//Drift
        left_x += xDrift;
        right_x += xDrift;
        left_y += yDrift;
        right_y += yDrift;
        
        //Save original values
        double orig_left_x = left_x;
        double orig_left_y = left_y;
        double orig_right_x = right_x;
        double orig_right_y = right_y;
        
        //Clamp x values to [0.0, 1.0].
        double screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
        double screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
        
        left_x = left_x / screenWidth;
        right_x = right_x / screenWidth;
        left_y = left_y / screenHeight;
        right_y = right_y / screenHeight;
        
        if (left_x >= 1.0)
            left_x = 1.0;
        else if (left_x <= 0.0)
        	left_x = 0.0;
        
        if (right_x >= 1.0)
            right_x = 1.0;
        else if (right_x <= 0.0)
            right_x = 0.0;

        //Clamp y values to [0.0, 1.0]
        if (left_y >= 1.0)
            left_y = 1.0;
        else if (left_y <= 0.0)
            left_y = 0.0;

        if (right_y >= 1.0)
            right_y = 1.0;
        else if (right_y <= 0.0)
            right_y = 0.0;

        double left_x_mod = left_x;
        double right_x_mod = right_x;
        double left_y_mod = left_y;
        double right_y_mod = right_y;
        
        try {
            Gaze gaze = new Gaze(left_x, right_x, left_y, right_y, 1, 1,
            		new Date(timestamp / 1000));
            if (recentGazes.size() >= 15)
                recentGazes.remove();
            recentGazes.add(gaze);

            for (Object curObj : recentGazes.toArray()) {
                Gaze curGaze = (Gaze) curObj;
                left_x_mod += curGaze.getLeftX();
                right_x_mod += curGaze.getRightX();
                left_y_mod += curGaze.getLeftY();
                right_y_mod += curGaze.getRightY();
            }
            left_x_mod /= recentGazes.size() + 1;
            right_x_mod /= recentGazes.size() + 1;
            left_y_mod /= recentGazes.size() + 1;
            right_y_mod /= recentGazes.size() + 1;

            Gaze modifiedGaze = new Gaze(left_x_mod, right_x_mod, left_y_mod,
                                         right_y_mod, 1, 1,
                                         new Date(timestamp / 1000));

            gaze_points.put(modifiedGaze);
        } catch (InterruptedException e) {
            //Ignore this point.
        }

        //Update crosshair position
        int screen_x = (int) ((orig_left_x + orig_right_x) / 2);
        int screen_y = (int) ((orig_left_y + orig_right_y) / 2);
        calibrator.moveCrosshair(screen_x, screen_y);
    }

}