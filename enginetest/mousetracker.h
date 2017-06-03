#ifndef MOUSETRACKER_H
#define MOUSETRACKER_H

#include <QCursor>
#include <QTimer>
#include <QLocalServer>
#include <QLocalSocket>
#include <QTcpServer>
#include <QTcpSocket>
#include <vector>
#include "gazeserver.h"

using namespace std;

class MouseTracker: QObject
{
    Q_OBJECT
    QPoint cursor;
    QTimer * timer;
    GazeServer* gazeServer;

private slots:


public:
    MouseTracker();
    ~MouseTracker();
    void start();
    void stop();


public slots:
    void trackMouse();


};

#endif // MOUSETRACKER_H
