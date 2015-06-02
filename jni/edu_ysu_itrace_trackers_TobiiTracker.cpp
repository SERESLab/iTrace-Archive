#include <ctime>
#include <tobii/sdk/cpp/Library.hpp>
#include <tobii/sdk/cpp/MainLoop.hpp>
#include <tobii/sdk/cpp/EyeTracker.hpp>
#include <tobii/sdk/cpp/EyeTrackerInfo.hpp>
#include <tobii/sdk/cpp/EyeTrackerException.hpp>
#include <tobii/sdk/cpp/EyeTrackerBrowser.hpp>
#include <tobii/sdk/cpp/EyeTrackerBrowserFactory.hpp>
#include <tobii/sdk/cpp/EyeTrackerFactory.hpp>
#include <tobii/sdk/cpp/GazeDataItem.hpp>
#include "edu_ysu_itrace_trackers_TobiiTracker.h"
#include "edu_ysu_itrace_trackers_TobiiTracker_BackgroundThread.h"
#include "edu_ysu_itrace_trackers_TobiiTracker_Calibrator.h"

using namespace tobii::sdk::cpp;

struct TobiiNativeData
{
	JavaVM* jvm;
	jobject j_tobii_tracker;
	jobject j_background_thread;
	MainLoop main_loop;
	EyeTracker::pointer_t eye_tracker;
};

//Only one TobiiTracker can be active at one time.
bool g_already_initialised = false;
//Sort of ugly but necessary. When connecting to eye tracker, this is used to
//pass the tracker information to the main thread.
EyeTrackerInfo::pointer_t g_et_info = EyeTrackerInfo::pointer_t();
//Also not very clean.
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
	Java_edu_ysu_itrace_trackers_TobiiTracker_00024BackgroundThread_jniBeginTobiiMainloop
	(JNIEnv* env, jobject obj)
{
	//Initialise Tobii SDK if not yet initialised, else an error condition has
	//occurred.
	if (g_already_initialised)
		return JNI_FALSE;
	else
	{
		tobii::sdk::cpp::Library::init();
		g_already_initialised = true;
	}

	//Get native data ByteBuffer field in TobiiTracker object.
	jfieldID jfid_parent = getFieldID(env, obj, "parent",
		"Ledu/ysu/itrace/trackers/TobiiTracker;");
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

void handleBrowserEvent(EyeTrackerBrowser::event_type_t type,
	EyeTrackerInfo::pointer_t et_info)
{
	if (type == EyeTrackerBrowser::TRACKER_FOUND)
		g_et_info = et_info;
}

JNIEXPORT jboolean JNICALL
	Java_edu_ysu_itrace_trackers_TobiiTracker_jniConnectTobiiTracker(
	JNIEnv* env, jobject obj, jint timeout_seconds)
{
	//Get native data from object.
	TobiiNativeData* native_data = getTobiiNativeData(env, obj);
	if (native_data == NULL)
		return JNI_FALSE;
	//Set TobiiTracker reference.
	native_data->j_tobii_tracker = env->NewGlobalRef(obj);

	//Find Tobii trackers.
	EyeTrackerBrowser::pointer_t browser =
		EyeTrackerBrowserFactory::createBrowser(native_data->main_loop);
	browser->start();
	browser->addEventListener(handleBrowserEvent);
	time_t start_time = time(NULL);
	//Wait until found or timeout occurs.
	while (g_et_info == NULL)
	{
		if (time(NULL) > start_time + timeout_seconds)
		{
			browser->stop();
			return JNI_FALSE;
		}
	}
	EyeTrackerInfo::pointer_t et_info = g_et_info;
	browser->stop();

	//Connect eye tracker
	native_data->eye_tracker = et_info->getEyeTrackerFactory()->
		createEyeTracker(native_data->main_loop);
	return JNI_TRUE;
}

JNIEXPORT void JNICALL Java_edu_ysu_itrace_trackers_TobiiTracker_close
	(JNIEnv* env, jobject obj)
{
	//Get native data from object.
	TobiiNativeData* native_data = getTobiiNativeData(env, obj);
	if (native_data == NULL)
	{
		throwJException(env, "java/lang/RuntimeException",
			"Cannot find native data.");
		return;
	}

	//Shut down main loop
	native_data->main_loop.quit();
}

void handleGazeData(GazeDataItem::pointer_t gaze_data)
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
		"newGazePoint", "(JDDDDIIDD)V");
	//Just pretend nothing happened.
	if (jmid_new_gaze_point == NULL)
		return;
	//Call newGazePoint.
	env->CallVoidMethod(obj, jmid_new_gaze_point, (jlong) gaze_data->timestamp,
		gaze_data->leftGazePoint2d.x, gaze_data->rightGazePoint2d.y,
		gaze_data->rightGazePoint2d.x, gaze_data->rightGazePoint2d.y,
		gaze_data->leftValidity, gaze_data->rightValidity,
		gaze_data->leftPupilDiameter, gaze_data->rightPupilDiameter);
}

JNIEXPORT void JNICALL Java_edu_ysu_itrace_trackers_TobiiTracker_startTracking
	(JNIEnv* env, jobject obj)
{
	//Do not continue if already tracking
	if (g_native_data_current != NULL)
	{
		throwJException(env, "java/io/IOException", "Already tracking.");
		return;
	}

	//Get native data from object.
	TobiiNativeData* native_data = getTobiiNativeData(env, obj);
	if (native_data == NULL)
	{
		throwJException(env, "java/lang/RuntimeException",
			"Cannot find native data.");
		return;
	}
	//Set native data for current tracking TobiiTracker.
	g_native_data_current = native_data;

	try
	{
		native_data->eye_tracker->startTracking();
		native_data->eye_tracker->addGazeDataReceivedListener(handleGazeData);
	}
	catch (EyeTrackerException e)
	{
		throwJException(env, "java/io/IOException", e.what());
		return;
	}
}

JNIEXPORT void JNICALL Java_edu_ysu_itrace_trackers_TobiiTracker_stopTracking
	(JNIEnv* env, jobject obj)
{
	try
	{
		g_native_data_current->eye_tracker->stopTracking();
		g_native_data_current = NULL;
	}
	catch (EyeTrackerException e)
	{
		throwJException(env, "java/io/IOException", e.what());
		return;
	}
}

JNIEXPORT void
	JNICALL Java_edu_ysu_itrace_trackers_TobiiTracker_00024Calibrator_jniAddPoint
	(JNIEnv* env, jobject obj, jdouble x, jdouble y)
{
	//Get native data from parent TobiiTracker
	jfieldID jfid_parent = getFieldID(env, obj, "parent",
		"Ledu/ysu/itrace/trackers/TobiiTracker;");
	if (jfid_parent == NULL)
	{
		throwJException(env, "java/lang/RuntimeException",
			"Parent TobiiTracker not found.");
		return;
	}
	jobject parent_tobii_tracker = env->GetObjectField(obj, jfid_parent);
	TobiiNativeData* native_data = getTobiiNativeData(env, parent_tobii_tracker);

	try
	{
		//Add new calibration point
		native_data->eye_tracker->addCalibrationPoint(Point2d(
			(double) x, (double) y));
	}
	catch (EyeTrackerException e)
	{
		throwJException(env, "java/io/IOException", e.what());
		return;
	}
}

JNIEXPORT void JNICALL
	Java_edu_ysu_itrace_trackers_TobiiTracker_00024Calibrator_jniStartCalibration
	(JNIEnv* env, jobject obj)
{
	//Get native data from parent TobiiTracker
	jfieldID jfid_parent = getFieldID(env, obj, "parent",
		"Ledu/ysu/itrace/trackers/TobiiTracker;");
	if (jfid_parent == NULL)
	{
		throwJException(env, "java/lang/RuntimeException",
			"Parent TobiiTracker not found.");
		return;
	}
	jobject parent_tobii_tracker = env->GetObjectField(obj, jfid_parent);
	TobiiNativeData* native_data = getTobiiNativeData(env, parent_tobii_tracker);

	try
	{
		//Start and clear calibration
		native_data->eye_tracker->startCalibration();
		native_data->eye_tracker->clearCalibration();
	}
	catch (EyeTrackerException e)
	{
		throwJException(env, "java/io/IOException", e.what());
		return;
	}
}

JNIEXPORT void JNICALL
	Java_edu_ysu_itrace_trackers_TobiiTracker_00024Calibrator_jniStopCalibration
	(JNIEnv* env, jobject obj)
{
	//Get native data from parent TobiiTracker
	jfieldID jfid_parent = getFieldID(env, obj, "parent",
		"Ledu/ysu/itrace/trackers/TobiiTracker;");
	if (jfid_parent == NULL)
	{
		throwJException(env, "java/lang/RuntimeException",
			"Parent TobiiTracker not found.");
		return;
	}
	jobject parent_tobii_tracker = env->GetObjectField(obj, jfid_parent);
	TobiiNativeData* native_data = getTobiiNativeData(env, parent_tobii_tracker);

	try
	{
		//Compute and stop calibration
		native_data->eye_tracker->computeCalibration();
		native_data->eye_tracker->stopCalibration();
	}
	catch (EyeTrackerException e)
	{
		throwJException(env, "java/io/IOException", e.what());
		return;
	}
}
	
	JNIEXPORT jintArray JNICALL
	Java_edu_ysu_itrace_trackers_TobiiTracker_00024Calibrator_jniGetCalibration
	(JNIEnv* env, jobject obj)
{
	//Get native data from parent TobiiTracker
	jfieldID jfid_parent = getFieldID(env, obj, "parent",
		"Ledu/ysu/itrace/trackers/TobiiTracker;");
	if (jfid_parent == NULL)
	{
		throwJException(env, "java/lang/RuntimeException",
			"Parent TobiiTracker not found.");
		return;
	}
	jobject parent_tobii_tracker = env->GetObjectField(obj, jfid_parent);
	TobiiNativeData* native_data = getTobiiNativeData(env, parent_tobii_tracker);

	try
	{
		//Get calibration
		Calibration::pointer_r calibrationData =
				native_data->eye_tracker->getCalibration();
		
		Calibration::plot_data_vector_t calibrationPlotData =
				calibrationData->getPlotData();
		
		int itemCount = static_cast<int>(calibrationPlotData->size());
		
		jintArray calibrationPoints = env->NewIntArray(env, 2 * itemCount);  // allocate
		
   		if (NULL == calibrationPoints) return NULL;
   		
   		jint *points = env->GetIntArrayElements(calibrationPoints, NULL);
   		
   		for (int i = 0; i < itemCount; i++)
    	{
        	Calibration::CalibrationPlotItem item = calibrationPlotData->at(i);
        	points[i] = item.truePosition.x;
        	points[itemCount+i] = item.truePosition.y;
        }
        env->ReleaseIntArrayElements(calibrationPoints, points, NULL);
        
        return calibrationPoints;
	}
	catch (EyeTrackerException e)
	{
		throwJException(env, "java/io/IOException", e.what());
		return;
	}
}
