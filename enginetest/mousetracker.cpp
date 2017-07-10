#include "mousetracker.h"
#include <QCursor>
#include <QDebug>
#include <QtNetwork>
#include <sstream>
#include <vector>

using namespace std;

MouseTracker::MouseTracker()
{
    gazeServer = GazeServer::getGazeServer();
    timer = new QTimer(this);
    connect(timer, SIGNAL(timeout()), this, SLOT(trackMouse()));
}

MouseTracker::~MouseTracker(){
    delete timer;
}

void MouseTracker::start(){
    timer->start(16);
}

void MouseTracker::stop(){
    timer->stop();
}

void MouseTracker::trackMouse(){
    cursor = QCursor::pos();
    stringstream ss;
    ss << "iTraceData," << cursor.x() << "," << cursor.y() << '\n';
    gazeServer->sendGazeData(ss.str().c_str(), ss.str().length()+1);
}

