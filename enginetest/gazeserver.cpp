#include "gazeserver.h"

GazeServer* GazeServer::gazeServer = 0;

GazeServer* GazeServer::getGazeServer(){
    if(!gazeServer){
        gazeServer = new GazeServer();
    }
    return gazeServer;
}

GazeServer::GazeServer()
{
    tcpServer = new QTcpServer(this);
    connect(tcpServer, SIGNAL(newConnection()), this, SLOT(newConnections()));
    if(!tcpServer->listen(QHostAddress::LocalHost, 8080)){
        return;
    }
    clientConnection = 0;
}

void GazeServer::newConnections(){
    while(tcpServer->hasPendingConnections()){
        QTcpSocket* client = tcpServer->nextPendingConnection();
        if(!client) continue;
        connect(client,SIGNAL(disconnected()), client, SLOT(deleteLater()));
        clients.push_back(client);
    }
}

void GazeServer::removedConnections(){

}

void GazeServer::sendGazeData(const char* data, int length){
    QByteArray block;
    QDataStream outStream(&block, QIODevice::WriteOnly);
    outStream.setVersion(QDataStream::Qt_5_8);
    int status = outStream.writeRawData(data,length);
    if(status == -1) return;
    for(int i=0; i<clients.size(); i++){
        QTcpSocket* client = clients[i];
        if(!client) continue;
        client->write(block);
        client->flush();
    }
}
