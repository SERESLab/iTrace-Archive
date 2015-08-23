#include <stdio.h>
#include <stdlib.h>
#include "TobiiGazeSDK/Common.h"
#include "TobiiGazeSDK/tobiigaze_discovery.h"
#include "TobiiGazeSDK/tobiigaze_calibration.h"
#include "edu_ysu_itrace_trackers_EyeXTracker.h"
#include "edu_ysu_itrace_trackers_EyeXTracker_BackgroundThread.h"
#include "edu_ysu_itrace_trackers_EyeXTracker_Calibrator.h"
#include <iostream>

struct EyeXNativeData
{
	JavaVM* jvm;
	jobject j_eye_tracker;
	jobject j_background_thread;
	tobiigaze_eye_tracker* eye_tracker;
};

TobiiNativeData* g_native_data_current = NULL;

void throwJException(JNIEnv* env, const char* jclass_name, const char* msg)
{
	jclass jclass = env->FindClass(jclass_name);
	env->ThrowNew(jclass, msg);
	env->DeleteLocalRef(jclass);
}

jfieldID getFieldID(JNIEnv* env, jobject obj, const char* name, const char* sig)
{
	jclass jclass = env->GetObjectClass(obj);
	if (jclass == NULL)
		return NULL;
	jfieldID jfid = env->GetFieldID(jclass, name, sig);
	if (jfid == NULL)
		return NULL;
	return jfid;
}

EyeXNativeData* getEyeXNativeData(JNIEnv* env, jobject obj)
{
	jfieldID jfid_native_data = getFieldID(env, obj, "native_data",
		"Ljava/nio/ByteBuffer;");
	if (jfid_native_data == NULL)
		return NULL;
	jobject native_data_bb = env->GetObjectField(obj, jfid_native_data);
	return (EyeXNativeData*) env->GetDirectBufferAddress(native_data_bb);
}

void on_gaze_data(const tobiigaze_gaze_data* gazedata, const tobiigaze_gaze_data_extensions* extensions, void *user_data) {

	JNIEnv* env = NULL;
	jint rs = g_native_data_current->jvm->GetEnv((void**) &env, JNI_VERSION_1_6);
	if (rs != JNI_OK || env == NULL)
		return;
	jobject obj = g_native_data_current->j_tobii_tracker;

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

	jclass eyex_tracker_class = global_env->GetObjectClass(global_obj);
	if (eyex_tracker_class == NULL)
	    return;
	jmethodID jmid_new_gaze_point = global_env->GetMethodID(eyex_tracker_class,
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
void on_error(tobiigaze_error_code error_code, void *user_data) { //need to throw exceptions and remove report_and_exit_on_error in future
    report_and_exit_on_error(error_code, tobiigaze_get_error_message(error_code));
    //TODO:Throw a JEyeTrackingException
}

void on_disconnect_callback(void *user_data) {
    printf("Disconnected.\n");
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

		tobiigaze_error_code error_code;

		const int urlSize = 256;
		char url[urlSize];

	    tobiigaze_get_connected_eye_tracker(url, urlSize, &error_code);
	    if (error_code) {
	    	printf("No eye tracker found.\n");
	    	return JNI_FALSE;
	    }

	    //Get native data ByteBuffer field in EyeXTracker object.
	    jfieldID jfid_parent = getFieldID(env, obj, "parent",
	    	"Ledu/ysu/itrace/trackers/EyeXTracker;");
	    if (jfid_parent == NULL)
	    	return JNI_FALSE;
	    jobject parent_eyex_tracker = env->GetObjectField(obj, jfid_parent);
	    jfieldID jfid_native_data = getFieldID(env, parent_eyex_tracker,
	    	"native_data", "Ljava/nio/ByteBuffer;");
	    if (jfid_native_data == NULL)
	    	return JNI_FALSE;
	    //Create structure to hold instance-specific data.
	    EyeXNativeData* native_data = new EyeXNativeData();
	    jobject native_data_bb = env->NewDirectByteBuffer((void*) native_data,
	    	sizeof(EyeXNativeData));
	    //Set java virtual machine and BackgroundThread reference.
	    env->GetJavaVM(&native_data->jvm);
	    native_data->j_background_thread = env->NewGlobalRef(obj);
	    //Store structure reference in Java object.
	    env->SetObjectField(parent_eyex_tracker, jfid_native_data, native_data_bb);
		
	    // Create an eye tracker instance.
	    native_data->eye_tracker = tobiigaze_create(url, &error_code);
	    report_and_exit_on_error(error_code, "tobiigaze_create");

	    // Enable diagnostic error reporting.
	    tobiigaze_register_error_callback(native_data->eye_tracker, on_error, NULL);

	    // Start the event loop. This must be done before connecting.
	    tobiigaze_run_event_loop(native_data->eye_tracker, &error_code);
	    report_and_exit_on_error(error_code, "tobiigaze_run_event_loop");

	    return JNI_TRUE;
}

//TRACKER FUNCTIONS
JNIEXPORT void JNICALL Java_edu_ysu_itrace_trackers_EyeXTracker_jniConnectEyeXTracker
  (JNIEnv *, jobject) {

	//Get native data from object.
	EyeXNativeData* native_data = getEyeXNativeData(env, obj);
	if (native_data == NULL) {
		throwJException(env, "java/lang/RuntimeException",
				"Cannot find native data.");
		return;
	}
	//Set EyeXTracker reference.
	native_data->j_eyex_tracker = env->NewGlobalRef(obj);

	// Connect to the tracker. The callback function is invoked when the
	// operation finishes, successfully or unsuccessfully.
	tobiigaze_connect_async(native_data->eye_tracker, &on_connect_callback, 0);
}

JNIEXPORT void JNICALL Java_edu_ysu_itrace_trackers_EyeXTracker_close
  (JNIEnv *, jobject) {

	//Get native data from object.
	EyeXNativeData* native_data = getEyeXNativeData(env, obj);
	if (native_data == NULL)
	{
		throwJException(env, "java/lang/RuntimeException",
			"Cannot find native data.");
		return;
	}

	// Disconnect.
	tobiigaze_disconnect_async(native_data->eye_tracker, &on_disconnect_callback, 0);

	// Break the event loop
	tobiigaze_break_event_loop(native_data->eye_tracker);

	// Clean up.
	tobiigaze_destroy(native_data->eye_tracker);

	delete native_data;
}

JNIEXPORT void JNICALL Java_edu_ysu_itrace_trackers_EyeXTracker_startTracking
  (JNIEnv *env, jobject obj) {

	//Do not continue if already tracking
	if (g_native_data_current != NULL)
	{
		throwJException(env, "java/io/IOException", "Already tracking.");
		return;
	}

	//Get native data from object.
	EyeXNativeData* native_data = getEyeXNativeData(env, obj);
	if (native_data == NULL)
	{
		throwJException(env, "java/lang/RuntimeException",
			"Cannot find native data.");
		return;
	}
	//Set native data for current tracking TobiiTracker.
	g_native_data_current = native_data;

	// Now that a connection is established,
	// start tracking.
	tobiigaze_start_tracking_async(native_data->eye_tracker, &start_tracking_callback, &on_gaze_data, 0);
}

JNIEXPORT void JNICALL Java_edu_ysu_itrace_trackers_EyeXTracker_stopTracking
  (JNIEnv *, jobject) {

	tobiigaze_stop_tracking_async(g_native_data_current->eye_tracker, &stop_tracking_callback, 0);
	g_native_data_current = NULL;
}

//CALIBRATION

//const uint8_t* calibration_data; //to retrieve the calib data //IGNORE FOR NOW
//uint32_t calibration_size; //to retrieve the calib data size //IGNORE FOR NOW

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

/*void get_calibration_handler(const struct tobiigaze_calibration *calibration, tobiigaze_error_code error_code, void *user_data) {

	if (error_code) {
		handle_calibration_error(error_code, user_data, "get_calibration_handler");
	    return;
	}
	calibration_data = calibration->data;
	calibration_size = calibration->actual_size;
}*/

JNIEXPORT void JNICALL Java_edu_ysu_itrace_trackers_EyeXTracker_00024Calibrator_jniAddPoint
  (JNIEnv *, jobject, jdouble x, jdouble y) {

	//Get native data from parent EyeXTracker
	jfieldID jfid_parent = getFieldID(env, obj, "parent",
		"Ledu/ysu/itrace/trackers/EyeXTracker;");
	if (jfid_parent == NULL)
	{
		throwJException(env, "java/lang/RuntimeException",
			"Parent EyeXTracker not found.");
		return;
	}
	jobject parent_eyex_tracker = env->GetObjectField(obj, jfid_parent);
	EyeXNativeData* native_data = getEyeXNativeData(env, parent_eyex_tracker);

	tobiigaze_point_2d* point;
	point->x = x;
	point->y = y;
	// The call to tobiigaze_calibration_add_point_async starts collecting calibration data at the specified point.
	// Make sure to keep the stimulus (i.e., the calibration dot) on the screen until the tracker is finished, that
	// is, until the callback function is invoked.
	tobiigaze_calibration_add_point_async(native_data->eye_tracker, point, add_calibration_point_handler, native_data->eye_tracker);
}

JNIEXPORT void JNICALL Java_edu_ysu_itrace_trackers_EyeXTracker_00024Calibrator_jniStartCalibration
  (JNIEnv *, jobject) {
	//Get native data from parent EyeXTracker
	jfieldID jfid_parent = getFieldID(env, obj, "parent",
		"Ledu/ysu/itrace/trackers/EyeXTracker;");
	if (jfid_parent == NULL)
	{
		throwJException(env, "java/lang/RuntimeException",
			"Parent EyeXTracker not found.");
		return;
	}
	jobject parent_eyex_tracker = env->GetObjectField(obj, jfid_parent);
	EyeXNativeData* native_data = getEyeXNativeData(env, parent_eyex_tracker);

	// calibration
	tobiigaze_calibration_start_async(native_data->eye_tracker, add_calibration_point_handler, native_data->eye_tracker);

}

JNIEXPORT void JNICALL Java_edu_ysu_itrace_trackers_EyeXTracker_00024Calibrator_jniStopCalibration
  (JNIEnv *, jobject) {

	//Get native data from parent EyeXTracker
	jfieldID jfid_parent = getFieldID(env, obj, "parent",
		"Ledu/ysu/itrace/trackers/EyeXTracker;");
	if (jfid_parent == NULL)
	{
		throwJException(env, "java/lang/RuntimeException",
			"Parent EyeXTracker not found.");
		return;
	}
	jobject parent_eyex_tracker = env->GetObjectField(obj, jfid_parent);
	EyeXNativeData* native_data = getEyeXNativeData(env, parent_eyex_tracker);

	printf("Computing calibration...\n");
	tobiigaze_calibration_compute_and_set_async(native_data->eye_tracker, compute_calibration_handler, native_data->eye_tracker);
	tobiigaze_calibration_stop_async(native_data->eye_tracker, stop_calibration_handler, native_data->eye_tracker);
}

JNIEXPORT jdoubleArray JNICALL Java_edu_ysu_itrace_trackers_EyeXTracker_00024Calibrator_jniGetCalibration
  (JNIEnv *, jobject) {
 //COMPLETELY IGNORE EVERYTHING BELOW FOR NOW. I NEED TO REWRITE THIS.
	/*//TODO: Finish this function!!!
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

