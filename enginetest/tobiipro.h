#ifndef TOBIIPRO_H
#define TOBIIPRO_H
#include <windows.h>
#include <sstream>
#include <cstdlib>
#include <QDebug>
#include "tobiifunctions.h"
class TobiiPro
{
private:
    static TobiiPro* tobiiPro;
    TobiiPro();
    HMODULE tobiiLibrary;
    void ExtractTobiiFunctions();
    void SetFunctionsToStubs();
public:
    static TobiiPro* getTobiiPro();
    FindAllTrackers* findAllTrackers;
    GetTrackerAddress* getTrackerAddress;
    GetTrackerName* getTrackerName;
    GetTrackerSerialNumber* getTrackerSerialNumber;
    GetTrackerModel* getTrackerModel;
    GetTrackerFirmwareVersion* getTrackerFirmwareVersion;
    GetTracker* getTracker;
    GetSystemTimestamp* getSystemTimestamp;
};

#endif // TOBIIPRO_H
