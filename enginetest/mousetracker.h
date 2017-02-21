#ifndef MOUSETRACKER_H
#define MOUSETRACKER_H

#include <QCursor>
#include <QTimer>
#include <QLocalServer>
#include <QLocalSocket>
#include <QTcpServer>
#include <QTcpSocket>
#include <vector>

using namespace std;

class MouseTracker: QObject
{
    Q_OBJECT
    //volatile bool running;
    QPoint cursor;
    QTimer * timer;
    QTcpServer * server;
    QTcpSocket * clientConnection;
    vector<QTcpSocket *> clients;

private slots:


public:
    MouseTracker();
    ~MouseTracker();
    void start();
    void stop();


public slots:
    void trackMouse();
    void newConnections();
    void removedConnection();


};

#endif // MOUSETRACKER_H
