#include "eyetracker.h"

CALIBRATION_OPERATION(calibrationOperationStub){
    return (TobiiResearchStatus)1;
}

EyeTracker::EyeTracker(){
    tobiiPro = TobiiPro::getTobiiPro();
    tobiiLibrary = tobiiPro->getTobiiLibrary();
    if(!tobiiLibrary) SetFunctionsToStubs();
    else ExtractTobiiFunctions();
}


EyeTracker::ExtractTobiiFunctions(){
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

EyeTracker::SetFunctionsToStubs(){

}
