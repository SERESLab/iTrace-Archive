#include "tobii_research.h"
#include "tobii_research_eyetracker.h"
#include "tobii_research_calibration.h"
#include <windows.h>


#ifndef TOBIIFUNCTIONS_H
#define TOBIIFUNCTIONS_H

#define FIND_ALL_TRACKERS(name) TobiiResearchStatus TOBII_RESEARCH_CALL name(TobiiResearchEyeTrackers ** eyetrackers)
typedef FIND_ALL_TRACKERS(FindAllTrackers);

#define GET_TRACKER(name) TobiiResearchStatus TOBII_RESEARCH_CALL name(const char* address, TobiiResearchEyeTracker** eyetracker)
typedef GET_TRACKER(GetTracker);

#define GET_TRACKER_ATTRIBUTE(name) TobiiResearchStatus TOBII_RESEARCH_CALL name(TobiiResearchEyeTracker * eyetracker, char** attribute)
typedef GET_TRACKER_ATTRIBUTE(GetTrackerAddress);
typedef GET_TRACKER_ATTRIBUTE(GetTrackerSerialNumber);
typedef GET_TRACKER_ATTRIBUTE(GetTrackerName);
typedef GET_TRACKER_ATTRIBUTE(GetTrackerModel);
typedef GET_TRACKER_ATTRIBUTE(GetTrackerFirmwareVersion);

#define GET_TRACKER_CAPABILITIES(name) TobiiResearchStatus TOBII_RESEARCH_CALL name(TobiiResearchEyeTracker* eyetracker, TobiiResearchCapabilities* capabilities)
typedef GET_TRACKER_CAPABILITIES(GetTrackerCapabilites);

#define GET_TRACKER_CALIBRATION_DATA(name) TobiiResearchStatus TOBII_RESEARCH_CALL name(TobiiResearchEyeTracker* eyetracker, TobiiResearchCalibrationData* data)
typedef GET_TRACKER_CALIBRATION_DATA(GetCalibrationData);

#define GET_TRACKER_GAZE_FREQUENCIES(name) TobiiResearchStatus TOBII_RESEARCH_CALL name(TobiiResearchEyeTracker* eyetracker, TobiiResearchGazeOutputFrequencies** frequencies)
typedef GET_TRACKER_GAZE_FREQUENCIES(GetGazeFrequencies);

#define GET_TRACKER_GAZE_FREQUENCY(name) TobiiResearchStatus TOBII_RESEARCH_CALL name(TobiiResearchEyeTracker* eyetracker, float* gaze_output_frequency)
typedef GET_TRACKER_GAZE_FREQUENCY(GetOutputFrequencies);

#define GET_TRACKER_TRACKING_MODES(name) TobiiResearchStatus TOBII_RESEARCH_CALL name(TobiiResearchEyeTracker* eyetracker, TobiiResearchEyeTrackingModes* modes)
typedef GET_TRACKER_TRACKING_MODES(GetTrackingModes);




#define CALIBRATION_OPERATION(name) TobiiResearchStatus TOBII_RESEARCH_CALL name(TobiiResearchEyeTracker* eyetracker)
typedef CALIBRATION_OPERATION(EnterCalibrationMode);
typedef CALIBRATION_OPERATION(LeaveCalibrationMode);

#define CALIBRATION_DATA_OPERATION(name) TobiiResearchStatus TOBII_RESEARCH_CALL name(TobiiResearchEyeTracker* eyetracker, float x, float y)
typedef CALIBRATION_DATA_OPERATION(CollectCalibrationData);
typedef CALIBRATION_DATA_OPERATION(DiscardCalibrationData);

#define COMPUTE_CALIBRATION(name) TobiiResearchStatus TOBII_RESEARCH_CALL name(TobiiResearchEyeTracker* eyetracker, TobiiResearchCalibrationResult** result)
typedef COMPUTE_CALIBRATION(ComputeCalibration);

#define GET_SYSTEM_TIMESTAMP(name) TobiiResearchStatus TOBII_RESEARCH_CALL name(int64_t * timestamp)
typedef GET_SYSTEM_TIMESTAMP(GetSystemTimestamp);

#endif // TOBIIFUNCTIONS_H
