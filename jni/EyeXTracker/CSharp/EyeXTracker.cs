using System;
using System.Threading;
using Tobii.Gaze.Core;

namespace EyeXTrackerGaze
{
    public class EyeXTracker
    {
        



        // BackgroundThread methods
        // native boolean jniBeginTobiiMainloop()
        //public bool jniBeginTobiiMainLoop()
        //{
        //    //return
        //}

        // Calibrator Methods
        //native void jniAddPoint(double x, double y)
        //native void jniStartCalibration()
        //native void jniStopCalibration()
        //native double[] jniGetCalibration()
        public void jniAddPoint(double x, double y)
        {
            
        }

        public void jniStartCalibration()
        {

        }

        public void jniStopCalibration()
        {

        }

        //public double[] jniGetCalibration()
        //{
        //    //return
        //}

        // EyeXTracker Methods
        //native boolean jniConnectTobiiTracker(int timeout_seconds)
        //native void close()
        //native void startTracking() throws exceptions
        //native void stopTracking() throws exceptions
        //public bool jniConnectTobiiTracker(int timeout_seconds)
        //{
        //    //return
        //}

        public void close()
        {

        }

        public void startTracking()
        {

        }

        public void stopTracking()
        {

        }
    }
}
