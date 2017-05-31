#ifndef MAINWINDOW_H
#define MAINWINDOW_H

#include "mousetracker.h"
#include <QMainWindow>


namespace Ui {
class MainWindow;
}

class MainWindow : public QMainWindow
{
    Q_OBJECT

public:
    explicit MainWindow(QWidget *parent = 0);
    ~MainWindow();

public slots:
    void startTracker();
    void displayMouse();


private:
    Ui::MainWindow *ui;
    MouseTracker * mouseTracker;
    QTcpSocket * socket;
    quint32 size;
};

#endif // MAINWINDOW_H
