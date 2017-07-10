#include "mainwindow.h"

#include <QtNetwork>
#include "ui_mainwindow.h"
#include "mousetracker.h"
#include "tobiiEyetracker.h"
#include "reticle.h"
#include <sstream>


using namespace std;

MainWindow::MainWindow(QWidget *parent) :
    QMainWindow(parent),
    ui(new Ui::MainWindow)
{
    mouseTracker = new MouseTracker();
    ui->setupUi(this);
    connect(ui->startServerButton, SIGNAL(released()), this, SLOT(startTracker()));
    socket = new QTcpSocket(this);
    connect(socket, SIGNAL(readyRead()), this, SLOT(displayMouse()));
    this->setFixedSize(this->geometry().width(),this->geometry().height());
    for(int i=0; i<tobiiEyeTracker::tobiiEyeTrackers.size(); i++){
        ui->trackerBox->addItem(tobiiEyeTracker::tobiiEyeTrackers[i]->getName());
    }
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
    char * data = new char[100];
    in.readRawData(data,100);
    ui->textBrowser->setText(data);
}
