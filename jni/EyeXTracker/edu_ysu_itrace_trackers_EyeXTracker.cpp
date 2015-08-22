#include <stdio.h>
#include <stdlib.h>
#include "TobiiGazeSDK/Common.h"
#include "TobiiGazeSDK/tobiigaze_discovery.h"
#include "TobiiGazeSDK/tobiigaze_calibration.h"
#include "edu_ysu_itrace_trackers_EyeXTracker.h"
#include "edu_ysu_itrace_trackers_EyeXTracker_BackgroundThread.h"
#include "edu_ysu_itrace_trackers_EyeXTracker_Calibrator.h"
#include <iostream>

/*
 * This is a simple example that demonstrates the asynchronous TobiiGazeCore calls.
 * It prints gaze data for 20 seconds.
 */

static tobiigaze_eye_tracker* eye_tracker;
static xcondition_variable disconnected_cv;

const int urlSize = 256;
char url[urlSize];
tobiigaze_error_code error_code;
xthread_handle hThread;

JNIEnv* global_env; //rough fix for on_gaze_data call back function
jobject global_obj; //rough fix for on_gaze_data call back function

// Prints gaze information, or "-" if gaze position could not be determined.
void on_gaze_data(const tobiigaze_gaze_data* gazedata, const tobiigaze_gaze_data_extensions* extensions, void *user_data) {
	int leftValidity;
	int rightValidity;

	if (gazedata->tracking_status == TOBIIGAZE_TRACKING_STATUS_BOTH_EYES_TRACKED ||
	        gazedata->tracking_status == TOBIIGAZE_TRACKING_STATUS_ONLY_LEFT_EYE_TRACKED ||
	        gazedata->tracking_status == TOBIIGAZE_TRACKING_STATUS_ONE_EYE_TRACKED_PROBABLY_LEFT) {
		leftValidity = 1;
	} else {
		leftValidity = 0;
	}

	if (gazedata->tracking_status == TOBIIGAZE_TRACKING_STATUS_BOTH_EYES_TRACKED ||
	         gazedata->tracking_status == TOBIIGAZE_TRACKING_STATUS_ONLY_RIGHT_EYE_TRACKED ||
	         gazedata->tracking_status == TOBIIGAZE_TRACKING_STATUS_ONE_EYE_TRACKED_PROBABLY_RIGHT) {
		rightValidity = 1;
	} else {
	    rightValidity = 0;
	}

	jclass tobii_tracker_class = global_env->GetObjectClass(global_obj);
	if (tobii_tracker_class == NULL)
	    return;
	jmethodID jmid_new_gaze_point = global_env->GetMethodID(tobii_tracker_class,
	    "newGazePoint", "(JDDDDIIDD)V");
	//Just pretend nothing happened.
	if (jmid_new_gaze_point == NULL)
	    return;

	//Call newGazePoint.
	global_env->CallVoidMethod(global_obj, jmid_new_gaze_point, (jlong) gazedata->timestamp,
	    gazedata->left.gaze_point_on_display_normalized.x, gazedata->left.gaze_point_on_display_normalized.y,
		gazedata->right.gaze_point_on_display_normalized.x, gazedata->right.gaze_point_on_display_normalized.y,
	    leftValidity, rightValidity,
	    -1, -1); //no pupil diameters for recording
}

// Error callback function.
void on_error(tobiigaze_error_code error_code, void *user_data) {
    report_and_exit_on_error(error_code, tobiigaze_get_error_message(error_code));
    //TODO:Throw a JEyeTrackingException
}

// Runs the event loop (blocking).
xthread_retval event_loop_thread_proc(void*) {
    tobiigaze_error_code error_code;

    tobiigaze_run_event_loop(eye_tracker, &error_code);
    report_and_exit_on_error(error_code, "tobiigaze_run_event_loop");

    THREADFUNC_RETURN(error_code);
}

void on_disconnect_callback(void *user_data) {
    printf("Disconnected.\n");

    // The tracker has been disconnected. Signal to the main thread that it is time to exit.
    xsignal_ready(&disconnected_cv);
}

void get_deviceinfo_callback(const tobiigaze_device_info* device_info, tobiigaze_error_code error_code, void *user_data) {
    report_and_exit_on_error(error_code, "get_deviceinfo_callback");
    printf("Serial number: %s\n", device_info->serial_number);
}

void stop_tracking_callback(tobiigaze_error_code error_code, void *user_data) {
    report_and_exit_on_error(error_code, "stop_tracking_callback");
    printf("Tracking stopped.\n");
}

void start_tracking_callback(tobiigaze_error_code error_code, void *user_data) {
    report_and_exit_on_error(error_code, "start_tracking_callback");
    printf("Tracking started.\n");
}

void on_connect_callback(tobiigaze_error_code error_code, void *user_data) {
    report_and_exit_on_error(error_code, "connect_callback");
    printf("Connected.\n");
}

//JNI FUNCTIONS

//THREADING
JNIEXPORT jboolean JNICALL Java_edu_ysu_itrace_trackers_EyeXTracker_00024BackgroundThread_jniBeginMainloop
  (JNIEnv *, jobject) {

	    xinitialize_cv(&disconnected_cv);

	    tobiigaze_get_connected_eye_tracker(url, urlSize, &error_code);
	    if (error_code) {
	    	printf("No eye tracker found.\n");
	    	exit(-1);
	    }
		
	    // Create an eye tracker instance.
	    eye_tracker = tobiigaze_create(url, &error_code);
	    report_and_exit_on_error(error_code, "tobiigaze_create");

	    // Enable diagnostic error reporting.
	    tobiigaze_register_error_callback(eye_tracker, on_error, NULL);

	    // Start the event loop. This must be done before connecting.
	    hThread = xthread_create(event_loop_thread_proc, eye_tracker);
	    
}

//TRACKER FUNCTIONS
JNIEXPORT void JNICALL Java_edu_ysu_itrace_trackers_EyeXTracker_jniConnectEyeXTracker
  (JNIEnv *, jobject) {

	// Connect to the tracker. The callback function is invoked when the
	// operation finishes, successfully or unsuccessfully.
	tobiigaze_connect_async(eye_tracker, &on_connect_callback, 0);
}

JNIEXPORT void JNICALL Java_edu_ysu_itrace_trackers_EyeXTracker_close
  (JNIEnv *, jobject) {

	// Disconnect.
	tobiigaze_disconnect_async(eye_tracker, &on_disconnect_callback, 0);

	// Wait until the tracker has been disconnected.
	if (!xwait_until_ready(&disconnected_cv)) {
		printf("Operation timed out.\n");
	    exit(-1);
	}

	// Break the event loop and join the event loop thread.
	tobiigaze_break_event_loop(eye_tracker);
	xthread_join(hThread);

	// Clean up.
	tobiigaze_destroy(eye_tracker);
}

JNIEXPORT void JNICALL Java_edu_ysu_itrace_trackers_EyeXTracker_startTracking
  (JNIEnv *env, jobject obj) {
	 global_env = env;
	 global_obj = obj;
	 // Now that a connection is established, retreive some device information
	 // and start tracking.
	 tobiigaze_get_device_info_async(eye_tracker, &get_deviceinfo_callback, 0);
	 tobiigaze_start_tracking_async(eye_tracker, &start_tracking_callback, &on_gaze_data, 0);
}

JNIEXPORT void JNICALL Java_edu_ysu_itrace_trackers_EyeXTracker_stopTracking
  (JNIEnv *, jobject) {

	tobiigaze_stop_tracking_async(eye_tracker, &stop_tracking_callback, 0);
}

//CALIBRATION

const uint8_t* calibration_data; //to retrieve the calib data
uint32_t calibration_size; //to retrieve the calib data size

// forward declarations
void add_calibration_point_handler(tobiigaze_error_code error_code, void *user_data);
void compute_calibration_handler(tobiigaze_error_code error_code, void *user_data);
void stop_calibration_handler(tobiigaze_error_code error_code, void *user_data);

void handle_calibration_error(tobiigaze_error_code error_code, void *user_data, const char *error_message) {
    if (error_code) {
        fprintf(stderr, "Error: %08X (%s).\n", error_code, error_message); //TODO: Throw a JCalibrationException
        tobiigaze_calibration_stop_async((tobiigaze_eye_tracker*) user_data, stop_calibration_handler, user_data);
    }
}

void compute_calibration_handler(tobiigaze_error_code error_code, void *user_data) {
    if (error_code) {
        if (error_code == TOBIIGAZE_FW_ERROR_OPERATION_FAILED) {
            fprintf(stderr, "Compute calibration FAILED due to insufficient gaze data.\n");
        }

        handle_calibration_error(error_code, user_data, "compute_calibration_handler");
        return;
    }

    printf("compute_calibration_handler: OK\n");

}

void add_calibration_point_handler(tobiigaze_error_code error_code, void *user_data) {

	if (error_code) {
        handle_calibration_error(error_code, user_data, "add_calibration_point_handler");
        return;
    }

    XSLEEP(2000);  // Give the user some time to move the gaze and focus on the object
}

void stop_calibration_handler(tobiigaze_error_code error_code, void *user_data) {

	if (error_code) {
        handle_calibration_error(error_code, user_data, "stop_calibration_handler");
        return;
    }

    printf("stop_calibration_handler: OK\n");
}

void get_calibration_handler(const struct tobiigaze_calibration *calibration, tobiigaze_error_code error_code, void *user_data) {

	if (error_code) {
		handle_calibration_error(error_code, user_data, "get_calibration_handler");
	    return;
	}
	calibration_data = calibration->data;
	calibration_size = calibration->actual_size;
}

JNIEXPORT void JNICALL Java_edu_ysu_itrace_trackers_EyeXTracker_00024Calibrator_jniAddPoint
  (JNIEnv *, jobject, jdouble x, jdouble y) {

	tobiigaze_point_2d* point;
	point->x = x;
	point->y = y;
	// The call to tobiigaze_calibration_add_point_async starts collecting calibration data at the specified point.
	// Make sure to keep the stimulus (i.e., the calibration dot) on the screen until the tracker is finished, that
	// is, until the callback function is invoked.
	tobiigaze_calibration_add_point_async(eye_tracker, point, add_calibration_point_handler, eye_tracker);
}

JNIEXPORT void JNICALL Java_edu_ysu_itrace_trackers_EyeXTracker_00024Calibrator_jniStartCalibration
  (JNIEnv *, jobject) {
	
	// calibration
	tobiigaze_calibration_start_async(eye_tracker, add_calibration_point_handler, eye_tracker);

}

JNIEXPORT void JNICALL Java_edu_ysu_itrace_trackers_EyeXTracker_00024Calibrator_jniStopCalibration
  (JNIEnv *, jobject) {

	printf("Computing calibration...\n");
	tobiigaze_calibration_compute_and_set_async(eye_tracker, compute_calibration_handler, eye_tracker);
	tobiigaze_calibration_stop_async(eye_tracker, stop_calibration_handler, eye_tracker);
}

JNIEXPORT jdoubleArray JNICALL Java_edu_ysu_itrace_trackers_EyeXTracker_00024Calibrator_jniGetCalibration
  (JNIEnv *, jobject) {
 //TODO: Finish this funtion!!!
	tobiigaze_get_calibration_async(eye_tracker, get_calibration_handler, eye_tracker);

	for (int i = 0; i < calibration_size; i++) {
		std::cout << calibration_data[i] << std::endl;
	}
	/*jdoubleArray calibrationPoints = env->NewDoubleArray(4 * calibration_size);  // allocate

	if (NULL == calibrationPoints) return NULL;

	jdouble *points = env->GetDoubleArrayElements(calibrationPoints, 0);

	CalibrationPlotItem item;
	for (int i = 0; i < itemCount; i++) {
	     item = calibrationPlotData->at(i);
	     points[i] = item.leftMapPosition.x;
	     points[itemCount+i] = item.leftMapPosition.y;
	     points[2*itemCount+i] = item.rightMapPosition.x;
	     points[3*itemCount+i] = item.rightMapPosition.y;
	}
	env->ReleaseDoubleArrayElements(calibrationPoints, points, 0);*/

	return NULL;//calibrationPoints;
}

