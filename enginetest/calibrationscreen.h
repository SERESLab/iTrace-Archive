#ifndef CALIBRATIONSCREEN_H
#define CALIBRATIONSCREEN_H

#include <QWidget>
#include <QPainter>
#include <QTimer>
#include <QDebug>
#include "tobiiEyetracker.h"
class CalibrationScreen : public QWidget
{
    Q_OBJECT
public:
    static CalibrationScreen* getCalibrationScreen();
    void startCalibration(Tracker* tracker);
signals:

public slots:
    void updatePosition();
protected:
    void paintEvent(QPaintEvent* event) override;

private:
    static CalibrationScreen* calibrationScreen;
    explicit CalibrationScreen(QWidget *parent = 0);
    QPointF points[9];
    QTimer * timer;
    Tracker* tracker;
    int t;
    int x;
    int y;
    int dotx;
    int doty;
    float size;
};

#endif // CALIBRATIONSCREEN_H
