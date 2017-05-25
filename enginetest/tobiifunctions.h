#include "tobii_research.h"
#include "tobii_research_eyetracker.h"


#ifndef TOBIIFUNCTIONS_H
#define TOBIIFUNCTIONS_H

#define FINDALLTRACKERS(name) TobiiResearchStatus TOBII_RESEARCH_CALL name(TobiiResearchEyeTrackers ** eyetrackers)
typedef FINDALLTRACKERS(FindAllTrackers);
FINDALLTRACKERS(FindAllTrackersStub)
{
    return (TobiiResearchStatus)1;
}
FindAllTrackers* findAllTrackers = FindAllTrackersStub;

#define GETTRACKERATTRIBUTE(name) TobiiResearchStatus TOBII_RESEARCH_CALL name(TobiiResearchEyeTracker * eyetracker, char** attribute)
GETTRACKERATTRIBUTE(GetTrackerAttributeStub){
    return (TobiiResearchStatus)1;
}
typedef GETTRACKERATTRIBUTE(GetTrackerAddress);
GetTrackerAddress* getTrackerAddress = GetTrackerAttributeStub;
typedef GETTRACKERATTRIBUTE(GetTrackerSerialNumber);
GetTrackerSerialNumber* getTrackerSerialNumber = GetTrackerAttributeStub;
typedef GETTRACKERATTRIBUTE(GetTrackerName);
GetTrackerName* getTrackerName = GetTrackerAttributeStub;
typedef GETTRACKERATTRIBUTE(GetTrackerModel);
GetTrackerModel* getTrackerModel = GetTrackerAttributeStub;
typedef GETTRACKERATTRIBUTE(GetTrackerFirmwareVersion);
GetTrackerFirmwareVersion* getTrackerFirmwareVersion = GetTrackerAttributeStub;

#define GETTRACKER(name) TobiiResearchStatus TOBII_RESEARCH_CALL name(const char* address, TobiiResearchEyeTracker** eyetracker)
GETTRACKER(GetTrackerStub){
    return (TobiiResearchStatus)1;
}
typedef GETTRACKER(GetTracker);
GetTracker* getTracker = GetTrackerStub;

#define GETSYSTEMTIMESTAMP(name) TobiiResearchStatus TOBII_RESEARCH_CALL name(int64_t * timestamp)
typedef GETSYSTEMTIMESTAMP(GetSystemTimestamp);
GETSYSTEMTIMESTAMP(GetSystemTimeStub)
{
    return (TobiiResearchStatus)0;
}
GetSystemTimestamp* getSystemTime = GetSystemTimeStub;



#endif // TOBIIFUNCTIONS_H
