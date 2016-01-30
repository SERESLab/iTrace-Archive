using System;
using System.Threading;
using Tobii.Gaze.Core;

namespace EyeXTrackerGaze
{
    public class EyeXTracker
    {
        private IEyeTracker tracker = null;
        private Thread thread = null;

        // BackgroundThread methods
        // native boolean jniBeginTobiiMainloop()
        public bool jniBeginTobiiMainLoop()
        {
            var thread = new Thread(() =>
            {
                try
                {
                    tracker.RunEventLoop();
                }
                catch (EyeTrackerException ex)
                {
                    Console.WriteLine("An error occurred in the eye tracker event loop: " + ex.Message);
                }

                Console.WriteLine("Leaving the event loop.");
            });

            thread.Start();

            return true;
        }

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
            if (thread != null)
            {
                tracker.BreakEventLoop();
                thread.Join();
            }

            if (tracker != null)
            {
                tracker.Dispose();
            }
        }

        public void startTracking()
        {

        }

        public void stopTracking()
        {

        }
    }
}
