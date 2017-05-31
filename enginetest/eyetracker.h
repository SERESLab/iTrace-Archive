#ifndef EYETRACKER_H
#define EYETRACKER_H
#include "tobii_research.h"
#include "tobii_research_eyetracker.h"
#include "tobiipro.h"
#include "tobiifunctions.h"
#include <windows.h>

class EyeTracker
{
    TobiiPro* tobiiPro;
    HMODULE tobiiLibrary;
    void ExtractTobiiFunctions();
    void SetFunctionsToStubs();
public:
    EyeTracker();
    void calibrate();
    GetTrackerAddress* getTrackerAddress;
    GetTrackerSerialNumber* getTrackerSerialNumber;
    GetTrackerName* getTrackerName;
    GetTrackerModel* getTrackerModel;
    GetTrackerFirmwareVersion* getTrackerFirmwareVersion;
    EnterCalibrationMode* enterCalibrationMode;
    LeaveCalibrationMode* leaveCalibrationMode;
    CollectCalibrationData* collectCalibrationData;
    DiscardCalibrationData* discardCalibrationData;
    ComputeCalibration* computeCalibration;
};

#endif // EYETRACKER_H
