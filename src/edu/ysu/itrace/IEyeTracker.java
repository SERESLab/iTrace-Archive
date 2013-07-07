package edu.ysu.itrace;

import java.io.IOException;
import edu.ysu.itrace.exceptions.CalibrationException;

public interface IEyeTracker {
    /**
    Closes eye tracker connection.
    */
    public void close();
    /**
    Clears all queued gaze data.
    */
    public void clear();
    /**
    Opens eye tracker calibration and exits after calibration completes.
    @throws CalibrationException if the eye tracker fails to calibrate or if
        there is a failure setting up the calibration environment.
    */
    public void calibrate() throws CalibrationException;
    /**
    Starts eye tracking and collecting data.
    @throws IOException Failed to start eye tracking.
    */
    public void startTracking() throws IOException;
    /**
    Stops eye tracking. Current data still available via getGaze, but no new
    gaze data is added.
    @throws IOException Failed to stop eye tracking.
    */
    public void stopTracking() throws IOException;
    /**
    Gets next stored gaze data, or null if no more exists.
    */
    public Gaze getGaze();
    /**
    Toggles whether crosshair should be displayed. Crosshair follows the current
    gaze on screen.
    @param enabled If true, display crosshair, else do not display crosshair.
    */
    public void displayCrosshair(boolean enabled);
}
