#include "mousetracker.h"

#include <QCursor>
#include <QDebug>
#include <QtNetwork>
#include <sstream>
#include <vector>

using namespace std;

MouseTracker::MouseTracker()
{
    server = new QTcpServer(this);
    connect(server, SIGNAL(newConnection()), this, SLOT(newConnections()));
    if(!server->listen(QHostAddress::LocalHost,8080)){
        return;
    }
    timer = new QTimer(this);
    clientConnection = NULL;
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
    QByteArray block;
    QDataStream outStream(&block, QIODevice::WriteOnly);
    outStream.setVersion(QDataStream::Qt_5_8);
    stringstream ss;
    qDebug() << 1;
    ss << "iTraceData," << cursor.x() << "," << cursor.y() << '\n';
    //qDebug() << ss.str().c_str();
    //qDebug() << outStream.status();
    int a = outStream.writeRawData(ss.str().c_str(), ss.str().length()+1);
    //if(a == -1)
    qDebug() << 1.5 << '\t'  << a;
    for(int i=0; i<clients.size(); i++){
        qDebug() << 2;
        QTcpSocket * client = clients[i];
        qDebug() << 3;
        client->write(block);
        qDebug() << 4;
        client->flush();
        qDebug() << 5;
    }
}

void MouseTracker::newConnections(){
    //qDebug() << (server->hasPendingConnections());
    while(server->hasPendingConnections()){
        QTcpSocket * client = server->nextPendingConnection();
        if(!client) continue;
        //qDebug() << "Have Client,";
        connect(client,SIGNAL(disconnected()), client, SLOT(deleteLater()));
        //qDebug() << "is connected";
        clients.push_back(client);
        //qDebug() << "added to clients";
    }
}

void MouseTracker::removedConnection(){

}
