#include "mainwindow.h"
#include <windows.h>
#include <QApplication>
#include <cstdint>
#include <cstdlib>
#include <cstring>
#include <sstream>
#include "tobiipro.h"
#include "calibrationscreen.h"
int main(int argc, char *argv[])
{
    QApplication a(argc, argv);
    TobiiResearchStatus result;
    TobiiResearchEyeTrackers* eyetrackers;
    TobiiPro* tobiiPro = TobiiPro::getTobiiPro();
    result = tobiiPro->findAllTrackers(&eyetrackers);
    MainWindow w;
    w.show();
    return a.exec();
}
