#ifndef TOBIIEYETRACKER_H
#define TOBIIEYETRACKER_H
#include "tobii_research.h"
#include "tobii_research_eyetracker.h"
#include "tobiipro.h"
#include "tracker.h"
#include <windows.h>
#include <vector>

using namespace std;

class tobiiEyeTracker: Tracker
{
    TobiiPro* tobiiPro;
    HMODULE tobiiLibrary;
    void ExtractTobiiFunctions();
    void SetFunctionsToStubs();
    TobiiResearchEyeTracker* eyetracker;
    GetTrackerAddress* getTrackerAddress;
    GetTrackerSerialNumber* getTrackerSerialNumber;
    GetTrackerName* getTrackerName;
    GetTrackerModel* getTrackerModel;
    GetTrackerFirmwareVersion* getTrackerFirmwareVersion;
    char* name;
    char* address;
    char* serialNumber;
    char* model;
    char* firmwareVersion;
    EnterCalibrationMode* enterCalibrationMode;
    LeaveCalibrationMode* leaveCalibrationMode;
    CollectCalibrationData* collectCalibrationData;
    DiscardCalibrationData* discardCalibrationData;
    ComputeCalibration* computeCalibration;
public:
    static vector<tobiiEyeTracker*> tobiiEyeTrackers;
    tobiiEyeTracker(TobiiResearchEyeTracker* eyetracker);
    void enterCalibration() override;
    void leaveCalibration() override;
    void useCalibrationPoint(float x, float y) override;
    void discardCalibrationPoint(float x, float y) override;
    char* getName();
    char* getAddress();
    char* getSerialNumber();
    char* getModel();
    char* getFirmwareVersion();
};

#endif // TOBIIEYETRACKER_H
