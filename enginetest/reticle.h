#ifndef RETICLE_H
#define RETICLE_H

#include <QWidget>
#include <QPainter>
#include <QPalette>

namespace Ui {
class Reticle;
}

class Reticle : public QWidget
{
    Q_OBJECT

public:
    explicit Reticle(QWidget *parent = 0);
    ~Reticle();

public slots:
    void moveReticle();
protected:
    void paintEvent(QPaintEvent *event) override;

private:
    Ui::Reticle *ui;
    QTcpSocket* socket;
};

#endif // RETICLE_H
