#include "sessionsetup.h"
#include "ui_sessionsetup.h"

sessionSetup::sessionSetup(QWidget *parent) :
    QDialog(parent),
    ui(new Ui::sessionSetup)
{
    ui->setupUi(this);
}

sessionSetup::~sessionSetup()
{
    delete ui;
}
