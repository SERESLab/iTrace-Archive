#include "tobiipro.h"

using namespace std;
FIND_ALL_TRACKERS(FindAllTrackersStub)
{
    return (TobiiResearchStatus)1;
}

GET_TRACKER(GetTrackerStub){
    return (TobiiResearchStatus)1;
}
GET_SYSTEM_TIMESTAMP(GetSystemTimeStub)
{
    return (TobiiResearchStatus)0;
}

TobiiPro* TobiiPro::tobiiPro = 0;

void TobiiPro::ExtractTobiiFunctions(){
    findAllTrackers = (FindAllTrackers *)GetProcAddress(tobiiLibrary, "tobii_research_find_all_eyetrackers");
    getTracker = (GetTracker*)GetProcAddress(tobiiLibrary,"tobii_research_get_eyetracker");
    getSystemTimestamp = (GetSystemTimestamp*)GetProcAddress(tobiiLibrary,"tobii_research_get_system_time_stamp");
}

void TobiiPro::SetFunctionsToStubs(){
    findAllTrackers = FindAllTrackersStub;
    getTracker = GetTrackerStub;
    getSystemTimestamp = GetSystemTimeStub;
}

TobiiPro::TobiiPro()
{
    stringstream path;
    char* tobiiHome = getenv("TOBII_HOME");
    if(tobiiHome != NULL) path << tobiiHome << "\\64\\lib\\tobii_research.dll";
    tobiiLibrary = LoadLibraryA(path.str().c_str());
    if(!tobiiLibrary) SetFunctionsToStubs();
    else ExtractTobiiFunctions();
}

TobiiPro* TobiiPro::getTobiiPro(){
    if(!tobiiPro){
        tobiiPro = new TobiiPro();
    }
    return tobiiPro;
}

HMODULE TobiiPro::getTobiiLibrary(){
    return tobiiLibrary;
}
