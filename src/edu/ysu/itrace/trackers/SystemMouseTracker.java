package edu.ysu.itrace.trackers;

import edu.ysu.itrace.*;
import edu.ysu.itrace.exceptions.CalibrationException;

import java.awt.Dimension;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.io.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Date;

/**
 * Tracker which follows the mouse cursor. Useful for testing when no eye
 * tracker is present.
 */
public class SystemMouseTracker extends Thread implements IEyeTracker {
    private enum RunState {
        RUNNING,
        STOPPING,
        STOPPED
    }

    private volatile RunState running = RunState.STOPPED;
    private LinkedBlockingQueue<Gaze> gazePoints
            = new LinkedBlockingQueue<Gaze>();

    public SystemMouseTracker() {
    }

    public void close() {
        try {
            stopTracking();
        } catch (IOException e) {
            //There's not really much that can be done at this point.
        }
    }

    public void clear() {
        gazePoints = new LinkedBlockingQueue<Gaze>();
    }

    public void calibrate() throws CalibrationException {
        //Already calibrated!
    }

    public void startTracking() throws IOException {
        start();
    }

    public void stopTracking() throws IOException {
        //Already stopped.
        if (running != RunState.RUNNING)
            return;

        running = RunState.STOPPING;
        while (running != RunState.STOPPED) {
            try {
                Thread.sleep(25);
            } catch (InterruptedException e) {
                //Just try again.
            }
        }
    }

    public Gaze getGaze() {
        return gazePoints.poll();
    }

    public void displayCrosshair(boolean enabled) {
        //Cursor itself should function as crosshair.
    }

    //Executed on separate thread to get mouse "gaze" data.
    public void run() {
        running = RunState.RUNNING;
        while (running == RunState.RUNNING)
        {
            Point cursorPosition = MouseInfo.getPointerInfo().getLocation();
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            Gaze gaze = new Gaze(
                    (double) cursorPosition.x / (double) screenSize.width,
                    (double) cursorPosition.y / (double) screenSize.height,
                    1.0, 1.0, new Date());
            gazePoints.add(gaze);
            try {
                Thread.sleep(25);
            } catch (InterruptedException e) {
                //Just try again.
            }
        }
        running = RunState.STOPPED;
    }
}
