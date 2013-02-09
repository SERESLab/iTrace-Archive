#include <ctime>
#include <tobii/sdk/cpp/init.hpp>
#include <tobii/sdk/cpp/mainloop.hpp>
#include <tobii/sdk/cpp/discovery/eyetracker-browser.hpp>
#include <tobii/sdk/cpp/discovery/factory-info.hpp>
#include <tobii/sdk/cpp/tracking/eyetracker-factory.hpp>
#include <tobii/sdk/cpp/tracking/gaze-data-item.hpp>
#include "edu_ysu_itrace_TobiiTracker.h"
#include "edu_ysu_itrace_TobiiTracker_BackgroundThread.h"
#include "edu_ysu_itrace_TobiiTracker_Calibrator.h"

using namespace tobii::sdk::cpp;

struct TobiiNativeData
{
	JavaVM* jvm;
	jobject j_tobii_tracker;
	jobject j_background_thread;
	mainloop main_loop;
	tracking::eyetracker::pointer eye_tracker;
};

//Only one TobiiTracker can be active at one time.
bool g_already_initialised = false;
//Sort of ugly but necessary. When connecting to eye tracker, this is used to
//pass the tracker information to the main thread.
discovery::eyetracker_info::pointer g_et_info =
	discovery::eyetracker_info::pointer();
//Also not very clean.
TobiiNativeData* g_native_data_current = NULL;

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

TobiiNativeData* getTobiiNativeData(JNIEnv* env, jobject obj)
{
	jfieldID jfid_native_data = getFieldID(env, obj, "native_data",
		"Ljava/nio/ByteBuffer;");
	if (jfid_native_data == NULL)
		return NULL;
	jobject native_data_bb = env->GetObjectField(obj, jfid_native_data);
	return (TobiiNativeData*) env->GetDirectBufferAddress(native_data_bb);
}

JNIEXPORT jboolean JNICALL
	Java_edu_ysu_itrace_TobiiTracker_00024BackgroundThread_jniBeginTobiiMainloop
	(JNIEnv* env, jobject obj)
{
	//Initialise Tobii SDK if not yet initialised, else an error condition has
	//occurred.
	if (g_already_initialised)
		return JNI_FALSE;
	else
	{
		tobii::sdk::cpp::init_library();
		g_already_initialised = true;
	}

	//Get native data ByteBuffer field in TobiiTracker object.
	jfieldID jfid_parent = getFieldID(env, obj, "parent",
		"Ledu/ysu/itrace/TobiiTracker;");
	if (jfid_parent == NULL)
		return JNI_FALSE;
	jobject parent_tobii_tracker = env->GetObjectField(obj, jfid_parent);
	jfieldID jfid_native_data = getFieldID(env, parent_tobii_tracker,
		"native_data", "Ljava/nio/ByteBuffer;");
	if (jfid_native_data == NULL)
		return JNI_FALSE;
	//Create structure to hold instance-specific data.
	TobiiNativeData* native_data = new TobiiNativeData();
	jobject native_data_bb = env->NewDirectByteBuffer((void*) native_data,
		sizeof(TobiiNativeData));
	//Set java virtual machine and BackgroundThread reference.
	env->GetJavaVM(&native_data->jvm);
	native_data->j_background_thread = env->NewGlobalRef(obj);
	//Store structure reference in Java object.
	env->SetObjectField(parent_tobii_tracker, jfid_native_data, native_data_bb);

	//Run!
	native_data->main_loop.run();

	//This code does not execute until the main loop has been stopped.
	delete native_data;

	return JNI_TRUE;
}

void handleBrowserEvent(discovery::eyetracker_browser::event_type type,
	discovery::eyetracker_info::pointer et_info)
{
	if (type == discovery::eyetracker_browser::TRACKER_FOUND)
		g_et_info = et_info;
}

JNIEXPORT jboolean JNICALL
	Java_edu_ysu_itrace_TobiiTracker_jniConnectTobiiTracker(JNIEnv* env,
	jobject obj, jint timeout_seconds)
{
	//Get native data from object.
	TobiiNativeData* native_data = getTobiiNativeData(env, obj);
	if (native_data == NULL)
		return JNI_FALSE;
	//Set TobiiTracker reference.
	native_data->j_tobii_tracker = env->NewGlobalRef(obj);

	//Find Tobii trackers.
	discovery::eyetracker_browser browser(native_data->main_loop);
	browser.start();
	browser.add_browser_event_listener(handleBrowserEvent);
	time_t start_time = time(NULL);
	//Wait until found or timeout occurs.
	while (g_et_info == NULL)
	{
		if (time(NULL) > start_time + timeout_seconds)
		{
			browser.stop();
			return JNI_FALSE;
		}
	}
	discovery::eyetracker_info::pointer et_info = g_et_info;
	browser.stop();

	//Connect eye tracker
	const discovery::factory_info fact_info(*et_info);
	native_data->eye_tracker =
		tracking::create_eyetracker(fact_info, native_data->main_loop);

	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_edu_ysu_itrace_TobiiTracker_close
	(JNIEnv* env, jobject obj)
{
	//Get native data from object.
	TobiiNativeData* native_data = getTobiiNativeData(env, obj);
	if (native_data == NULL)
		return JNI_FALSE;

	//Shut down main loop
	native_data->main_loop.quit();

	return JNI_TRUE;
}

void handleGazeData(tracking::gaze_data_item::pointer gaze_data)
{
	JNIEnv* env = NULL;
	jint rs = g_native_data_current->jvm->GetEnv((void**) &env, JNI_VERSION_1_6);
	if (rs != JNI_OK || env == NULL)
		return;
	jobject obj = g_native_data_current->j_tobii_tracker;

	jclass tobii_tracker_class = env->GetObjectClass(obj);
	if (tobii_tracker_class == NULL)
		return;
	jmethodID jmid_new_gaze_point = env->GetMethodID(tobii_tracker_class,
		"newGazePoint", "(JDDDD)V");
	//Just pretend nothing happened.
	if (jmid_new_gaze_point == NULL)
		return;
	//Call newGazePoint.
	env->CallVoidMethod(obj, jmid_new_gaze_point, (jlong) gaze_data->time_stamp,
		gaze_data->left_gaze_point_2d.x, gaze_data->right_gaze_point_2d.y,
		gaze_data->right_gaze_point_2d.x, gaze_data->right_gaze_point_2d.y);
}

JNIEXPORT jboolean JNICALL Java_edu_ysu_itrace_TobiiTracker_startTracking
	(JNIEnv* env, jobject obj)
{
	//Do not continue if already tracking
	if (g_native_data_current != NULL)
		return JNI_FALSE;

	//Get native data from object.
	TobiiNativeData* native_data = getTobiiNativeData(env, obj);
	if (native_data == NULL)
		return JNI_FALSE;
	//Set native data for current tracking TobiiTracker.
	g_native_data_current = native_data;

	native_data->eye_tracker->start_tracking();
	native_data->eye_tracker->add_gaze_data_received_listener(handleGazeData);

	return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_edu_ysu_itrace_TobiiTracker_stopTracking
	(JNIEnv *, jobject)
{
	g_native_data_current->eye_tracker->stop_tracking();
	g_native_data_current = NULL;
	return JNI_TRUE;
}

JNIEXPORT void
	JNICALL Java_edu_ysu_itrace_TobiiTracker_00024Calibrator_jniAddPoint
	(JNIEnv* env, jobject obj, jdouble x, jdouble y)
{
	//Get native data from parent TobiiTracker
	jfieldID jfid_parent = getFieldID(env, obj, "parent",
		"Ledu/ysu/itrace/TobiiTracker;");
	if (jfid_parent == NULL)
		return;
	jobject parent_tobii_tracker = env->GetObjectField(obj, jfid_parent);
	TobiiNativeData* native_data = getTobiiNativeData(env, parent_tobii_tracker);

	//Add new calibration point
	native_data->eye_tracker->add_calibration_point(tracking::point_2d((double) x,
		(double) y));
}

JNIEXPORT void JNICALL
	Java_edu_ysu_itrace_TobiiTracker_00024Calibrator_jniStartCalibration
	(JNIEnv* env, jobject obj)
{
	//Get native data from parent TobiiTracker
	jfieldID jfid_parent = getFieldID(env, obj, "parent",
		"Ledu/ysu/itrace/TobiiTracker;");
	if (jfid_parent == NULL)
		return;
	jobject parent_tobii_tracker = env->GetObjectField(obj, jfid_parent);
	TobiiNativeData* native_data = getTobiiNativeData(env, parent_tobii_tracker);

	//Start and clear calibration
	native_data->eye_tracker->start_calibration();
	native_data->eye_tracker->clear_calibration();
}

JNIEXPORT jboolean JNICALL
	Java_edu_ysu_itrace_TobiiTracker_00024Calibrator_jniStopCalibration
	(JNIEnv* env, jobject obj)
{
	//Get native data from parent TobiiTracker
	jfieldID jfid_parent = getFieldID(env, obj, "parent",
		"Ledu/ysu/itrace/TobiiTracker;");
	if (jfid_parent == NULL)
		return JNI_FALSE;
	jobject parent_tobii_tracker = env->GetObjectField(obj, jfid_parent);
	TobiiNativeData* native_data = getTobiiNativeData(env, parent_tobii_tracker);

	try
	{
		//Compute and stop calibration
		native_data->eye_tracker->compute_calibration();
		native_data->eye_tracker->stop_calibration();
	}
	catch (eyetracker_exception e)
	{
		return JNI_FALSE;
	}
	return JNI_TRUE;
}
