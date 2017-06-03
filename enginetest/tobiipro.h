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
    HMODULE tobiiLibrary;
    TobiiPro();
    void ExtractTobiiFunctions();
    void SetFunctionsToStubs();

public:
    HMODULE getTobiiLibrary();
    static TobiiPro* getTobiiPro();
    FindAllTrackers* findAllTrackers;

    GetTracker* getTracker;
    GetSystemTimestamp* getSystemTimestamp;
};

#endif // TOBIIPRO_H
