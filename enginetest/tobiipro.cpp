#include "tobiipro.h"

using namespace std;
FINDALLTRACKERS(FindAllTrackersStub)
{
    return (TobiiResearchStatus)1;
}
GETTRACKERATTRIBUTE(GetTrackerAttributeStub){
    return (TobiiResearchStatus)1;
}
GETTRACKER(GetTrackerStub){
    return (TobiiResearchStatus)1;
}
GETSYSTEMTIMESTAMP(GetSystemTimeStub)
{
    return (TobiiResearchStatus)0;
}

TobiiPro* TobiiPro::tobiiPro = 0;

void TobiiPro::ExtractTobiiFunctions(){
    findAllTrackers = (FindAllTrackers *)GetProcAddress(tobiiLibrary, "tobii_research_find_all_eyetrackers");
    getTrackerAddress = (GetTrackerAddress *)GetProcAddress(tobiiLibrary,"tobii_research_get_address");
    getTrackerName = (GetTrackerName *)GetProcAddress(tobiiLibrary, "tobii_research_get_device_name");
    getTrackerSerialNumber = (GetTrackerSerialNumber *)GetProcAddress(tobiiLibrary,"tobii_reseach_get_serial_number");
    getTrackerModel = (GetTrackerModel *)GetProcAddress(tobiiLibrary, "tobii_research_get_model");
    getTrackerFirmwareVersion = (GetTrackerFirmwareVersion*)GetProcAddress(tobiiLibrary, "tobii_research_get_firmware_version");
    getTracker = (GetTracker*)GetProcAddress(tobiiLibrary,"tobii_research_get_eyetracker");
    getSystemTimestamp = (GetSystemTimestamp*)GetProcAddress(tobiiLibrary,"tobii_research_get_system_time_stamp");
}

void TobiiPro::SetFunctionsToStubs(){
    findAllTrackers = FindAllTrackersStub;
    getTrackerAddress = GetTrackerAttributeStub;
    getTrackerName = GetTrackerAttributeStub;
    getTrackerSerialNumber = GetTrackerAttributeStub;
    getTrackerModel = GetTrackerAttributeStub;
    getTrackerFirmwareVersion = GetTrackerAttributeStub;
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
