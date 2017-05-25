#include "tobii_research.h"
#include "tobii_research_eyetracker.h"
#include <windows.h>


#ifndef TOBIIFUNCTIONS_H
#define TOBIIFUNCTIONS_H

#define FINDALLTRACKERS(name) TobiiResearchStatus TOBII_RESEARCH_CALL name(TobiiResearchEyeTrackers ** eyetrackers)
typedef FINDALLTRACKERS(FindAllTrackers);

#define GETTRACKERATTRIBUTE(name) TobiiResearchStatus TOBII_RESEARCH_CALL name(TobiiResearchEyeTracker * eyetracker, char** attribute)
typedef GETTRACKERATTRIBUTE(GetTrackerAddress);
typedef GETTRACKERATTRIBUTE(GetTrackerSerialNumber);
typedef GETTRACKERATTRIBUTE(GetTrackerName);
typedef GETTRACKERATTRIBUTE(GetTrackerModel);
typedef GETTRACKERATTRIBUTE(GetTrackerFirmwareVersion);

#define GETTRACKER(name) TobiiResearchStatus TOBII_RESEARCH_CALL name(const char* address, TobiiResearchEyeTracker** eyetracker)
typedef GETTRACKER(GetTracker);

#define GETSYSTEMTIMESTAMP(name) TobiiResearchStatus TOBII_RESEARCH_CALL name(int64_t * timestamp)
typedef GETSYSTEMTIMESTAMP(GetSystemTimestamp);

#endif // TOBIIFUNCTIONS_H
