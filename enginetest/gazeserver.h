#ifndef GAZESERVER_H
#define GAZESERVER_H

#include <QLocalServer>
#include <QLocalSocket>
#include <QTcpServer>
#include <QTcpSocket>
#include <QtNetwork>
#include <vector>

using namespace std;

class GazeServer: QObject
{
    Q_OBJECT
private:
    GazeServer();
    static GazeServer* gazeServer;
    QTcpServer* tcpServer;
    QTcpSocket* clientConnection;
    vector<QTcpSocket*> clients;
public:
    static GazeServer* getGazeServer();
    void sendGazeData(const char* data, int length);
public slots:
    void newConnections();
    void removedConnections();
};

#endif // GAZESERVER_H
