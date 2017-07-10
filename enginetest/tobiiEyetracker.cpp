#include "tobiiEyetracker.h"

CALIBRATION_OPERATION(calibrationOperationStub){
    return (TobiiResearchStatus)1;
}
GET_TRACKER_ATTRIBUTE(GetTrackerAttributeStub){
    *attribute = "Tobii Pro SDK not found!";
    return (TobiiResearchStatus)1;
}

vector<tobiiEyeTracker*> tobiiEyeTracker::tobiiEyeTrackers;

tobiiEyeTracker::tobiiEyeTracker(TobiiResearchEyeTracker* eyetracker){
    this->eyetracker = eyetracker;
    tobiiPro = TobiiPro::getTobiiPro();
    tobiiLibrary = tobiiPro->getTobiiLibrary();
    if(!tobiiLibrary) SetFunctionsToStubs();
    else ExtractTobiiFunctions();

}

void tobiiEyeTracker::ExtractTobiiFunctions(){
    getTrackerAddress = (GetTrackerAddress *)GetProcAddress(tobiiLibrary,"tobii_research_get_address");
    getTrackerName = (GetTrackerName *)GetProcAddress(tobiiLibrary, "tobii_research_get_device_name");
    getTrackerSerialNumber = (GetTrackerSerialNumber *)GetProcAddress(tobiiLibrary,"tobii_reseach_get_serial_number");
    getTrackerModel = (GetTrackerModel *)GetProcAddress(tobiiLibrary, "tobii_research_get_model");
    getTrackerFirmwareVersion = (GetTrackerFirmwareVersion*)GetProcAddress(tobiiLibrary, "tobii_research_get_firmware_version");
    enterCalibrationMode = (EnterCalibrationMode*)GetProcAddress(tobiiLibrary,"tobii_research_screen_based_calibration_enter_calibration_mode");
    leaveCalibrationMode = (LeaveCalibrationMode*)GetProcAddress(tobiiLibrary,"tobii_reserach_screen_based_calibration_leave_calibration_mode");
    collectCalibrationData = (CollectCalibrationData*)GetProcAddress(tobiiLibrary,"tobii_research_screen_based_calibration_collect_data");
    discardCalibrationData = (DiscardCalibrationData*)GetProcAddress(tobiiLibrary,"tobii_research_screen_based_calibration_discard_data");
    computeCalibration = (ComputeCalibration*)GetProcAddress(tobiiLibrary,"tobii_research_screen_based_calibration_compute_and_apply");
}

void tobiiEyeTracker::SetFunctionsToStubs(){

}

void tobiiEyeTracker::enterCalibration(){
    enterCalibrationMode(eyetracker);
}

void tobiiEyeTracker::leaveCalibration(){
    leaveCalibrationMode(eyetracker);
}

void tobiiEyeTracker::useCalibrationPoint(float x, float y){
    collectCalibrationData(eyetracker, x, y);
}

void tobiiEyeTracker::discardCalibrationPoint(float x, float y){
    discardCalibrationData(eyetracker, x, y);
}

char* tobiiEyeTracker::getName(){
    return name;
}

char* tobiiEyeTracker::getAddress(){
    return address;
}

char* tobiiEyeTracker::getSerialNumber(){
    return serialNumber;
}

char* tobiiEyeTracker::getModel(){
    return model;
}

char* tobiiEyeTracker::getFirmwareVersion(){
    return firmwareVersion;
}
