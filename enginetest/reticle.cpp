#include "reticle.h"
#include "ui_reticle.h"

Reticle::Reticle(QWidget *parent) :
    QWidget(parent),
    ui(new Ui::Reticle)
{
    ui->setupUi(this);
    setAttribute(Qt::WA_TranslucentBackground);
    setAttribute(Qt::WA_QuitOnClose, false);
    this->setWindowFlags(Qt::FramelessWindowHint|Qt::WindowStaysOnTopHint);
    socket = new QTcpSocket(this);
    connect(socket, SIGNAL(readyRead()), this, SLOT(move()));
}

Reticle::~Reticle()
{
    delete ui;
}

void Reticle::paintEvent(QPaintEvent* /*event*/){
    QPainter painter(this);
    painter.setRenderHint(QPainter::Antialiasing, true);
    QColor red(255,0,0);
    QPen pen(red);
    pen.setWidth(5);
    painter.setBrush(Qt::NoBrush);
    painter.setPen(pen);
    painter.drawEllipse(5,5,geometry().width()-10,geometry().height()-10);
}

void Reticle::moveReticle(){
    QDataStream in(socket);
    in.setVersion(QDataStream::Qt_5_8);
    char* data = new char[100];
    in.readRawData(data,100);

}
