package edu.ysu.itrace;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import edu.ysu.itrace.exceptions.CalibrationException;
import edu.ysu.itrace.exceptions.EyeTrackerConnectException;

public class EyeTrackerFactory {

    public static ArrayList<IEyeTracker> getAvailableEyeTrackers() {
        //TODO read a config file and use reflection to
        //  build each of the trackers in the arraylist
        return null;
    }

    public static IEyeTracker getConcreteEyeTracker(int i) throws
            EyeTrackerConnectException, CalibrationException {
        return new TobiiTracker();

        /*return new IEyeTracker(){

            @Override
            public Gaze getGaze() {
                Random r = new Random();
                return new Gaze(r.nextDouble(), r.nextDouble(), new Date());
            }

            @Override
            public void close() {
                // TODO Auto-generated method stub
            }

            @Override
            public void startTracking() {
                // TODO Auto-generated method stub
            }

            @Override
            public void stopTracking() {
                // TODO Auto-generated method stub
            }};
        //return getAvailableEyeTrackers().get(i);
         */
    }
}
