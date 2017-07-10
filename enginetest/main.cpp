#include "mainwindow.h"
#include <windows.h>
#include <QApplication>
#include <cstdint>
#include <cstdlib>
#include <cstring>
#include <sstream>
#include "tobiipro.h"
#include "calibrationscreen.h"
#include "reticle.h"

using namespace std;

int main(int argc, char *argv[])
{
    QApplication a(argc, argv);
    TobiiResearchStatus result;
    TobiiResearchEyeTrackers* eyetrackers;
    TobiiPro* tobiiPro = TobiiPro::getTobiiPro();
    result = tobiiPro->findAllTrackers(&eyetrackers);
    if(result != TOBII_RESEARCH_STATUS_FATAL_ERROR){
        for(int i=0; i<eyetrackers->count; i++){
            tobiiEyeTracker::tobiiEyeTrackers.push_back(new tobiiEyeTracker(eyetrackers->eyetrackers[i]));
        }
    }else{
        qDebug() << "Tobii Pro SDK not loaded." << endl;
    }
    MainWindow w;
    w.show();
    Reticle r((QWidget*)w.parent());
    r.show();
    return a.exec();
}
