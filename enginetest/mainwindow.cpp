#include "mainwindow.h"
#include <windows.h>
#include <QtNetwork>
#include "ui_mainwindow.h"
#include "mousetracker.h"
#include <sstream>

using namespace std;

MainWindow::MainWindow(QWidget *parent) :
    QMainWindow(parent),
    ui(new Ui::MainWindow)
{
    mouseTracker = new MouseTracker();
    ui->setupUi(this);
    connect(ui->pushButton, SIGNAL(released()), this, SLOT(startTracker()));
    socket = new QTcpSocket(this);
    connect(socket, SIGNAL(readyRead()), this, SLOT(displayMouse()));
}

MainWindow::~MainWindow()
{
    delete mouseTracker;
    delete socket;
    delete ui;
}

void MainWindow::startTracker(){
    mouseTracker->start();
    size = 0;
    socket->connectToHost("localhost",8080,QIODevice::ReadOnly);

}

void MainWindow::displayMouse(){
    QDataStream in(socket);
    in.setVersion(QDataStream::Qt_5_8);
    quint32 mousex;
    quint32 mousey;
    //in >> mousex;
    //in >> mousey;
    char * data = new char[100];
    in.readRawData(data,100);
    //stringstream ss;
    //ss << mousex << '\t' << mousey;
    ui->textBrowser->setText(data);
}
