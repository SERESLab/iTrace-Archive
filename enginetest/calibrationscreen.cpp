#include "calibrationscreen.h"




CalibrationScreen::CalibrationScreen(QWidget *parent) : QWidget(parent){
    resize(1000,1000);
    points[0] = QPointF(0.1,0.1);
    points[1] = QPointF(0.5,0.1);
    points[2] = QPointF(0.9,0.1);
    points[3] = QPointF(0.1,0.5);
    points[4] = QPointF(0.5,0.5);
    points[5] = QPointF(0.9,0.5);
    points[6] = QPointF(0.1,0.9);
    points[7] = QPointF(0.5,0.9);
    points[8] = QPointF(0.9,0.9);
    t = 0;
    timer = new QTimer(this);

    connect(timer, SIGNAL(timeout()), this, SLOT(updatePosition()));
    timer->start(10);
}

void CalibrationScreen::paintEvent(QPaintEvent * /*event*/){
    size = (int)(50.0-(0.5*(float)(t%100)));
    x = (int)((points[t/100].x()*(float)width())-(size/2));
    y = (int)((points[t/100].y()*(float)height())-(size/2));

    QPainter painter(this);
    painter.setRenderHint(QPainter::Antialiasing, true);
    painter.setBrush(Qt::red);
    painter.setPen(Qt::red);
    painter.drawEllipse(x,y,size,size);
}

void CalibrationScreen::updatePosition(){
    t++;
    t %= 900;
    update();
}
