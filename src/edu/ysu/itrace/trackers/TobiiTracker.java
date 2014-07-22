package edu.ysu.itrace.trackers;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

import edu.ysu.itrace.*;
import edu.ysu.itrace.exceptions.CalibrationException;
import edu.ysu.itrace.exceptions.EyeTrackerConnectException;

public class TobiiTracker implements IEyeTracker {
    private static class BackgroundThread extends Thread {
        private TobiiTracker parent = null;

        public BackgroundThread(TobiiTracker parent) {
            this.parent = parent;
        }

        public void run() {
            //TODO: Handle error condition
            jniBeginTobiiMainloop();
        }

        private native boolean jniBeginTobiiMainloop();
    }

    private static class Calibrator extends edu.ysu.itrace.Calibrator {
        private TobiiTracker parent = null;

        public Calibrator(TobiiTracker tracker) throws IOException {
            super();
            parent = tracker;
        }

        protected void startCalibration() throws Exception {
            jniStartCalibration();
        }

        protected void stopCalibration() throws Exception {
            jniStopCalibration();
        }

        protected void useCalibrationPoint(double x, double y)
                throws Exception {
            jniAddPoint(x, y);
        }

        private native void jniAddPoint(double x, double y)
                throws RuntimeException, IOException;
        private native void jniStartCalibration() throws RuntimeException,
                IOException;
        private native void jniStopCalibration() throws RuntimeException,
                IOException;
    }

    private BackgroundThread bg_thread = null;
    private volatile ByteBuffer native_data = null;
    private LinkedBlockingQueue<Gaze> gaze_points =
            new LinkedBlockingQueue<Gaze>();
    private LinkedBlockingQueue<Gaze> recentGazes =
            new LinkedBlockingQueue<Gaze>();
    private Calibrator calibrator;
    private double xDrift = 0, yDrift = 0;

    static { System.loadLibrary("TobiiTracker"); }

    public TobiiTracker() throws EyeTrackerConnectException,
                                 IOException {
        calibrator = new Calibrator(this);
        //Initialise the background thread which functions as the main loop in
        //the Tobii SDK.
        bg_thread = new BackgroundThread(this);
        bg_thread.start();
        while (native_data == null); //Wait until background thread sets native_data
        if (!jniConnectTobiiTracker(10)) {
            this.close();
            throw new EyeTrackerConnectException();
        }
    }

    public static void main(String[] args) {
        TobiiTracker tobii_tracker = null;
        try {
            tobii_tracker = new TobiiTracker();
            System.out.println("Connected successfully to eyetracker.");

            Dimension window_bounds = Toolkit.getDefaultToolkit()
                                      .getScreenSize();
            System.out.println("Screen size: (" + window_bounds.width + ", "
                    + window_bounds.height + ")");
            try {
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
            while ((new Date()).getTime() < start + 25000) {
                Gaze gaze = tobii_tracker.getGaze();
                if (gaze != null) {
                    System.out.println("Gaze at " + gaze.getTrackerTime() + ": ("
                            + (int) (gaze.getX() * window_bounds.width) + ", "
                            + (int) (gaze.getY() * window_bounds.height)
                            + ") with validity (Left: " + gaze.getLeftValidity()
                            + ", Right: " + gaze.getRightValidity() + ")");
                }
            }
            tobii_tracker.stopTracking();

            tobii_tracker.close();
        } catch (EyeTrackerConnectException e) {
            System.out.println("Failed to connect to Tobii eyetracker.");
        } catch (CalibrationException e) {
            tobii_tracker.close();
            System.out.println("Could not calibrate. Try again.");
        } catch (IOException e) {
            tobii_tracker.close();
            System.out.println("IO failure occurred.");
        }
        System.out.println("Done!");
    }

    public void clear() {
        gaze_points = new LinkedBlockingQueue<Gaze>();
    }

    public void calibrate() throws CalibrationException {
        calibrator.calibrate();
    }

    public Gaze getGaze() {
        return gaze_points.poll();
    }

    public void setXDrift(int drift) {
        xDrift = ((double) drift) / 100;
    }

    public void setYDrift(int drift) {
        yDrift = ((double) drift) / 100;
    }

    public void newGazePoint(long timestamp, double left_x, double left_y,
            double right_x, double right_y, int left_validity,
            int right_validity, double left_pupil_diameter,
            double right_pupil_diameter) {
        //Drift
        left_x += xDrift;
        right_x += xDrift;
        left_y += yDrift;
        right_y += yDrift;

        //Average left and right eyes for each value.
        double x = (left_x + right_x) / 2;
        double y = (left_y + right_y) / 2;

        //Clamp x values to [0.0, 1.0].
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

        double gaze_left_validity = 1.0 - ((double) left_validity / 4.0);
        double gaze_right_validity = 1.0 - ((double) right_validity / 4.0);

        double left_x_mod = left_x,
               right_x_mod = right_x,
               left_y_mod = left_y,
               right_y_mod = right_y;
        try {
            Gaze gaze = new Gaze(left_x, right_x, left_y, right_y,
                                 gaze_left_validity, gaze_right_validity,
                                 left_pupil_diameter, right_pupil_diameter,
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
                    right_y_mod, gaze_left_validity, gaze_right_validity,
                    left_pupil_diameter, right_pupil_diameter,
                    new Date(timestamp / 1000));

            gaze_points.put(modifiedGaze);
        } catch (InterruptedException e) {
            //Ignore this point.
        }

        Dimension screen_size = Toolkit.getDefaultToolkit().getScreenSize();
        int screen_x =
                (int) (screen_size.width * ((left_x_mod + right_x_mod) / 2));
        int screen_y =
                (int) (screen_size.height * ((left_y_mod + right_y_mod) / 2));
        calibrator.moveCrosshair(screen_x, screen_y);
    }

    private native boolean jniConnectTobiiTracker(int timeout_seconds);
    public native void close();
    public native void startTracking() throws RuntimeException, IOException;
    public native void stopTracking() throws RuntimeException, IOException;

    public void displayCrosshair(boolean enabled) {
        calibrator.displayCrosshair(enabled);
    }
}
