// This is the main DLL file.

//#include "stdafx.h"

#include "edu_ysu_itrace_trackers_EyeXTracker.h"
#include "edu_ysu_itrace_trackers_EyeXTracker_BackgroundThread.h"
#include "edu_ysu_itrace_trackers_EyeXTracker_Calibrator.h"
#include <msclr/auto_gcroot.h>

using namespace System::Reflection;
using namespace System;
using namespace EyeXTrackerGaze;   // once we have the C# library compiled and added as a reference
using System::Runtime::InteropServices::Marshal;

#pragma region iTrace Java Specific


struct EyeXNativeData
{
	JavaVM* jvm;
	jobject j_eye_tracker;
	jobject j_background_thread;
	
	//EyeXTracker^ *eye_tracker;  
	gcroot<EyeXTracker^> eye_tracker; // This is a pointer to the C# eye tracker
	//ErrorCode create_error_code;
};

EyeXNativeData* g_native_data_current = NULL;
bool add_point_callback = false;

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
	return (EyeXNativeData*)env->GetDirectBufferAddress(native_data_bb);
}

/*
void on_gaze_data(const tobiigaze_gaze_data* gazedata, const tobiigaze_gaze_data_extensions* extensions, void *user_data) {

	JNIEnv* env = NULL;
	jint rs = g_native_data_current->jvm->GetEnv((void**)&env, JNI_VERSION_1_6);
	if (rs != JNI_OK || env == NULL)
		return;
	jobject obj = g_native_data_current->j_eye_tracker;

	int leftValidity;
	int rightValidity;

	if (gazedata->tracking_status == TOBIIGAZE_TRACKING_STATUS_BOTH_EYES_TRACKED ||
		gazedata->tracking_status == TOBIIGAZE_TRACKING_STATUS_ONLY_LEFT_EYE_TRACKED ||
		gazedata->tracking_status == TOBIIGAZE_TRACKING_STATUS_ONE_EYE_TRACKED_PROBABLY_LEFT) {
		leftValidity = 1;
	}
	else {
		leftValidity = 0;
	}

	if (gazedata->tracking_status == TOBIIGAZE_TRACKING_STATUS_BOTH_EYES_TRACKED ||
		gazedata->tracking_status == TOBIIGAZE_TRACKING_STATUS_ONLY_RIGHT_EYE_TRACKED ||
		gazedata->tracking_status == TOBIIGAZE_TRACKING_STATUS_ONE_EYE_TRACKED_PROBABLY_RIGHT) {
		rightValidity = 1;
	}
	else {
		rightValidity = 0;
	}

	jclass eyex_tracker_class = env->GetObjectClass(obj);
	if (eyex_tracker_class == NULL)
		return;
	jmethodID jmid_new_gaze_point = env->GetMethodID(eyex_tracker_class,
		"newGazePoint", "(JDDDDIIDD)V");
	//Just pretend nothing happened.
	if (jmid_new_gaze_point == NULL)
		return;

	int pupilDiameter = 0; //EyeX does not record pupil diameter
						   //jni doesn't like sending straight numbers not stored in a variable.
						   //Call newGazePoint.
	env->CallVoidMethod(obj, jmid_new_gaze_point, (jlong)gazedata->timestamp,
		gazedata->left.gaze_point_on_display_normalized.x, gazedata->left.gaze_point_on_display_normalized.y,
		gazedata->right.gaze_point_on_display_normalized.x, gazedata->right.gaze_point_on_display_normalized.y,
		leftValidity, rightValidity,
		pupilDiameter, pupilDiameter); //no pupil diameters for recording
}
*/
#pragma endregion

#pragma region JNI Functions
/*
* Class:     edu_ysu_itrace_trackers_EyeXTracker_BackgroundThread
* Method:    jniBeginMainloop
* Signature: ()Z
*/
JNIEXPORT jboolean JNICALL Java_edu_ysu_itrace_trackers_EyeXTracker_00024BackgroundThread_jniBeginMainloop
(JNIEnv * env, jobject obj)
{
	//Initialize SDK?

	//Get native data ByteBuffer field in EyeXTracker object.
	jfieldID jfid_parent = getFieldID(env, obj, "parent",
		"Ledu/ysu/itrace/trackers/EyeXTracker;");
	if (jfid_parent == NULL)
		return JNI_FALSE;

	//create c++ object with jfid_parent
	jobject parent_eyex_tracker = env->GetObjectField(obj, jfid_parent);

	//get obj field - "native_data" from TobiiTracker obj "parent"
	jfieldID jfid_native_data = getFieldID(env, parent_eyex_tracker,
		"native_data", "Ljava/nio/ByteBuffer;");
	if (jfid_native_data == NULL)
		return JNI_FALSE;

	//Create structure to hold instance-specific data.
	EyeXNativeData* native_data = new EyeXNativeData();

	//create structure reference
	jobject native_data_bb = env->NewDirectByteBuffer((void*)native_data,
		sizeof(EyeXNativeData));

	//Set java virtual machine and BackgroundThread reference.
	env->GetJavaVM(&native_data->jvm);
	native_data->j_background_thread = env->NewGlobalRef(obj);

	//Store structure reference in Java object.
	env->SetObjectField(parent_eyex_tracker, jfid_native_data, native_data_bb);

	//Run!
	native_data->eye_tracker->jniBeginTobiiMainLoop();

	//This code does not execute until the main loop has been stopped.
	delete native_data;

	return JNI_TRUE;
}

/*
* Class:     edu_ysu_itrace_trackers_EyeXTracker_Calibrator
* Method:    jniAddPoint
* Signature: (DD)V
*/
JNIEXPORT void JNICALL Java_edu_ysu_itrace_trackers_EyeXTracker_00024Calibrator_jniAddPoint
(JNIEnv * env, jobject obj, jdouble x, jdouble y)
{

}

/*
* Class:     edu_ysu_itrace_trackers_EyeXTracker_Calibrator
* Method:    jniStartCalibration
* Signature: ()V
*/
JNIEXPORT void JNICALL Java_edu_ysu_itrace_trackers_EyeXTracker_00024Calibrator_jniStartCalibration
(JNIEnv * env, jobject obj)
{

}

/*
* Class:     edu_ysu_itrace_trackers_EyeXTracker_Calibrator
* Method:    jniStopCalibration
* Signature: ()V
*/
JNIEXPORT void JNICALL Java_edu_ysu_itrace_trackers_EyeXTracker_00024Calibrator_jniStopCalibration
(JNIEnv * env, jobject obj)
{

}

/*
* Class:     edu_ysu_itrace_trackers_EyeXTracker_Calibrator
* Method:    jniGetCalibration
* Signature: ()[D
*/
JNIEXPORT jdoubleArray JNICALL Java_edu_ysu_itrace_trackers_EyeXTracker_00024Calibrator_jniGetCalibration
(JNIEnv * env, jobject obj)
{
	jdoubleArray calibrationPoints = {};
	return calibrationPoints;
}

/*
* Class:     edu_ysu_itrace_trackers_EyeXTracker
* Method:    jniConnectEyeXTracker
* Signature: ()Z
*/
JNIEXPORT jboolean JNICALL Java_edu_ysu_itrace_trackers_EyeXTracker_jniConnectEyeXTracker
(JNIEnv * env, jobject obj)
{
	return true;
}

/*
* Class:     edu_ysu_itrace_trackers_EyeXTracker
* Method:    close
* Signature: ()V
*/
JNIEXPORT void JNICALL Java_edu_ysu_itrace_trackers_EyeXTracker_close
(JNIEnv * env, jobject obj)
{
	//close
	//eye_tracker->close();
}

/*
* Class:     edu_ysu_itrace_trackers_EyeXTracker
* Method:    startTracking
* Signature: ()V
*/
JNIEXPORT void JNICALL Java_edu_ysu_itrace_trackers_EyeXTracker_startTracking
(JNIEnv * env, jobject obj)
{

}

/*
* Class:     edu_ysu_itrace_trackers_EyeXTracker
* Method:    stopTracking
* Signature: ()V
*/
JNIEXPORT void JNICALL Java_edu_ysu_itrace_trackers_EyeXTracker_stopTracking
(JNIEnv * env, jobject obj)
{

}

#pragma endregion

#pragma region Reference
///*
//* Class:     CsharpConsumer
//* Method:    addTwoNos
//* Signature: (II)I
//*/
//JNIEXPORT jint JNICALL Java_CsharpConsumer_addTwoNos
//(JNIEnv * env, jobject jobj, jint a, jint b)
//{
//	//Adder^ ad = gcnew Adder();
//	//int res = (int)ad->addTwoNumbers(a, b);
//	//return res;
//}
#pragma endregion

#pragma region Register .Net Assembly -- required
ref class Register
{
public:
	//path to the newly compiled C# library -- this must be set !
	static String^ pathBase = "C:\\Users\\Brent\\Documents\\Visual Studio 2015\\Projects\\MyCSharpMathsLib\\MyCSharpMathsLib\\bin\\Debug";

	static void setPathBase(String^ path) {
		pathBase = path;
	}
	/*
	Following code is copied from
	http://social.msdn.microsoft.com/Forums/en/vcgeneral/thread/780eb1b2-05be-42d3-8315-4ae82bb79e71
	*/
	static Assembly^ MyResolveEventHandler(Object^ sender, ResolveEventArgs^ args)
	{
		//This handler is called only when the common language runtime tries to bind to the assembly and fails.
		printf("AssemblyResolve Handler called");
		Console::WriteLine("current path :" + pathBase);
		//Retrieve the list of referenced assemblies in an array of AssemblyName.
		Assembly^ MyAssembly;
		Assembly^ objExecutingAssemblies;
		String^ strTempAssmbPath = "";
		
		objExecutingAssemblies = Assembly::GetExecutingAssembly();
		array<AssemblyName ^>^ arrReferencedAssmbNames = objExecutingAssemblies->GetReferencedAssemblies();

		//Loop through the array of referenced assembly names.
		for each (AssemblyName^ strAssmbName in arrReferencedAssmbNames)
		{
			//Check for the assembly names that have raised the "AssemblyResolve" event.
			if (strAssmbName->FullName->Substring(0, strAssmbName->FullName->IndexOf(",")) == args->Name->Substring(0, args->Name->IndexOf(",")))
			{
				//Build the path of the assembly from where it has to be loaded.				
				strTempAssmbPath = pathBase + args->Name->Substring(0, args->Name->IndexOf(",")) + ".dll";
				break;
			}

		}
		//Load the assembly from the specified path. 					
		MyAssembly = Assembly::LoadFrom(strTempAssmbPath);

		//Return the loaded assembly.
		return MyAssembly;
	}
};

/*
* Class:     com_microsoft_pdw_loaderclient_LoaderClientNativeBridge
* Method:    reigsterAssemblyHandler
* Signature: (Ljava/lang/String;)I

This method tells our bridge code from where to load .Net assemblies.
Custom loading of .net aseeblies frees us from registering assemblies.
*/
JNIEXPORT jint JNICALL Java_edu_ysu_itrace_trackers_EyeXTracker_registerAssemblyHandler
(JNIEnv * env, jobject jobj, jstring str)
{
	AppDomain^ currentDomain = AppDomain::CurrentDomain;
	const char* ttt = (env)->GetStringUTFChars(str, NULL);
	String^ tmp = Marshal::PtrToStringAnsi((IntPtr)((char*)ttt));
	Register::setPathBase(tmp);
	(env)->ReleaseStringUTFChars(str, ttt);
	currentDomain->AssemblyResolve += gcnew ResolveEventHandler(Register::MyResolveEventHandler);
	printf("Registered AssemblyResolve Handler with");
	Console::Write(tmp);
	return 0;
}
#pragma endregion


