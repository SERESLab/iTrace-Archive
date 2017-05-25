#include "mainwindow.h"
#include "tobii_research.h"
#include "tobii_research_eyetracker.h"
#include "tobiifunctions.h"
#include <windows.h>
#include <QApplication>
#include <cstdint>

int main(int argc, char *argv[])
{
    TobiiResearchEyeTrackers* eyetrackers = NULL;
    HMODULE tobiiLibrary = LoadLibraryA("C:\\Users\\bsharif-administrato\\Desktop\\TobiiPro\\TobiiPro\\64\\lib\\tobii_research.dll");
    if(!tobiiLibrary){
        qDebug() << "Failed to load Tobii Research DLL.";
        return 1;
    }
    findAllTrackers = (FindAllTrackers *)GetProcAddress(tobiiLibrary, "tobii_research_find_all_eyetrackers");
    TobiiResearchStatus result = findAllTrackers(&eyetrackers);
    qDebug() << (result == TOBII_RESEARCH_STATUS_OK);
    qDebug() << eyetrackers->count;
    if(eyetrackers->count != 0){
        char * trackerName;
        getTrackerName = (GetTrackerName*)GetProcAddress(tobiiLibrary, "tobii_research_get_device_name");
        TobiiResearchEyeTracker* eyetracker = eyetrackers->eyetrackers[0];
        if(!getTrackerName){
            qDebug() << "Failed to load tobii_research_get_device_name.";
            return 1;
        }
        result = getTrackerName(eyetracker, &trackerName);
        qDebug() << result;
        qDebug() << trackerName;
    }
    getSystemTime = (GetSystemTimestamp *)GetProcAddress(tobiiLibrary,"tobii_research_get_system_time_stamp");
    int64_t time = 0;
    result = getSystemTime(&time);
    qDebug() << (result == TOBII_RESEARCH_STATUS_OK);

    QApplication a(argc, argv);
    MainWindow w;
    w.show();

    return a.exec();
}
