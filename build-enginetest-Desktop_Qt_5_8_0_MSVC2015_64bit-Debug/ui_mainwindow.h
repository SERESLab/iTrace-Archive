/********************************************************************************
** Form generated from reading UI file 'mainwindow.ui'
**
** Created by: Qt User Interface Compiler version 5.8.0
**
** WARNING! All changes made in this file will be lost when recompiling UI file!
********************************************************************************/

#ifndef UI_MAINWINDOW_H
#define UI_MAINWINDOW_H

#include <QtCore/QVariant>
#include <QtWidgets/QAction>
#include <QtWidgets/QApplication>
#include <QtWidgets/QButtonGroup>
#include <QtWidgets/QCheckBox>
#include <QtWidgets/QComboBox>
#include <QtWidgets/QFrame>
#include <QtWidgets/QHeaderView>
#include <QtWidgets/QLabel>
#include <QtWidgets/QMainWindow>
#include <QtWidgets/QMenu>
#include <QtWidgets/QMenuBar>
#include <QtWidgets/QPushButton>
#include <QtWidgets/QSpinBox>
#include <QtWidgets/QStatusBar>
#include <QtWidgets/QTextBrowser>
#include <QtWidgets/QToolBar>
#include <QtWidgets/QWidget>

QT_BEGIN_NAMESPACE

class Ui_MainWindow
{
public:
    QWidget *centralWidget;
    QPushButton *startServerButton;
    QTextBrowser *textBrowser;
    QComboBox *trackerBox;
    QLabel *label;
    QPushButton *calibrateButton;
    QSpinBox *xdriftSpin;
    QSpinBox *ydriftSpin;
    QLabel *label_2;
    QLabel *label_3;
    QFrame *line;
    QPushButton *sessionButton;
    QCheckBox *xmlCheck;
    QCheckBox *jsonCheck;
    QFrame *line_2;
    QLabel *label_4;
    QCheckBox *reticleBox;
    QMenuBar *menuBar;
    QMenu *menuEngine_GUI;
    QToolBar *mainToolBar;
    QStatusBar *statusBar;

    void setupUi(QMainWindow *MainWindow)
    {
        if (MainWindow->objectName().isEmpty())
            MainWindow->setObjectName(QStringLiteral("MainWindow"));
        MainWindow->resize(689, 192);
        centralWidget = new QWidget(MainWindow);
        centralWidget->setObjectName(QStringLiteral("centralWidget"));
        startServerButton = new QPushButton(centralWidget);
        startServerButton->setObjectName(QStringLiteral("startServerButton"));
        startServerButton->setGeometry(QRect(430, 60, 75, 23));
        textBrowser = new QTextBrowser(centralWidget);
        textBrowser->setObjectName(QStringLiteral("textBrowser"));
        textBrowser->setGeometry(QRect(520, 60, 151, 21));
        textBrowser->setVerticalScrollBarPolicy(Qt::ScrollBarAlwaysOff);
        trackerBox = new QComboBox(centralWidget);
        trackerBox->setObjectName(QStringLiteral("trackerBox"));
        trackerBox->setGeometry(QRect(60, 20, 121, 21));
        label = new QLabel(centralWidget);
        label->setObjectName(QStringLiteral("label"));
        label->setGeometry(QRect(10, 10, 61, 41));
        calibrateButton = new QPushButton(centralWidget);
        calibrateButton->setObjectName(QStringLiteral("calibrateButton"));
        calibrateButton->setGeometry(QRect(10, 50, 75, 23));
        xdriftSpin = new QSpinBox(centralWidget);
        xdriftSpin->setObjectName(QStringLiteral("xdriftSpin"));
        xdriftSpin->setGeometry(QRect(10, 90, 42, 22));
        ydriftSpin = new QSpinBox(centralWidget);
        ydriftSpin->setObjectName(QStringLiteral("ydriftSpin"));
        ydriftSpin->setGeometry(QRect(100, 90, 42, 22));
        label_2 = new QLabel(centralWidget);
        label_2->setObjectName(QStringLiteral("label_2"));
        label_2->setGeometry(QRect(60, 90, 51, 21));
        label_3 = new QLabel(centralWidget);
        label_3->setObjectName(QStringLiteral("label_3"));
        label_3->setGeometry(QRect(150, 90, 61, 21));
        line = new QFrame(centralWidget);
        line->setObjectName(QStringLiteral("line"));
        line->setGeometry(QRect(200, 0, 20, 241));
        line->setFrameShape(QFrame::VLine);
        line->setFrameShadow(QFrame::Sunken);
        sessionButton = new QPushButton(centralWidget);
        sessionButton->setObjectName(QStringLiteral("sessionButton"));
        sessionButton->setGeometry(QRect(240, 20, 101, 23));
        xmlCheck = new QCheckBox(centralWidget);
        xmlCheck->setObjectName(QStringLiteral("xmlCheck"));
        xmlCheck->setGeometry(QRect(310, 60, 70, 17));
        jsonCheck = new QCheckBox(centralWidget);
        jsonCheck->setObjectName(QStringLiteral("jsonCheck"));
        jsonCheck->setGeometry(QRect(310, 80, 70, 17));
        line_2 = new QFrame(centralWidget);
        line_2->setObjectName(QStringLiteral("line_2"));
        line_2->setGeometry(QRect(400, 0, 20, 191));
        line_2->setFrameShape(QFrame::VLine);
        line_2->setFrameShadow(QFrame::Sunken);
        label_4 = new QLabel(centralWidget);
        label_4->setObjectName(QStringLiteral("label_4"));
        label_4->setGeometry(QRect(230, 60, 81, 21));
        reticleBox = new QCheckBox(centralWidget);
        reticleBox->setObjectName(QStringLiteral("reticleBox"));
        reticleBox->setGeometry(QRect(100, 50, 91, 21));
        MainWindow->setCentralWidget(centralWidget);
        menuBar = new QMenuBar(MainWindow);
        menuBar->setObjectName(QStringLiteral("menuBar"));
        menuBar->setGeometry(QRect(0, 0, 689, 21));
        menuEngine_GUI = new QMenu(menuBar);
        menuEngine_GUI->setObjectName(QStringLiteral("menuEngine_GUI"));
        MainWindow->setMenuBar(menuBar);
        mainToolBar = new QToolBar(MainWindow);
        mainToolBar->setObjectName(QStringLiteral("mainToolBar"));
        MainWindow->addToolBar(Qt::TopToolBarArea, mainToolBar);
        statusBar = new QStatusBar(MainWindow);
        statusBar->setObjectName(QStringLiteral("statusBar"));
        MainWindow->setStatusBar(statusBar);

        menuBar->addAction(menuEngine_GUI->menuAction());

        retranslateUi(MainWindow);

        QMetaObject::connectSlotsByName(MainWindow);
    } // setupUi

    void retranslateUi(QMainWindow *MainWindow)
    {
        MainWindow->setWindowTitle(QApplication::translate("MainWindow", "iTrace Engine", Q_NULLPTR));
        startServerButton->setText(QApplication::translate("MainWindow", "Start Server", Q_NULLPTR));
        label->setText(QApplication::translate("MainWindow", "Tracker", Q_NULLPTR));
        calibrateButton->setText(QApplication::translate("MainWindow", "Calibrate", Q_NULLPTR));
        label_2->setText(QApplication::translate("MainWindow", "X Drift", Q_NULLPTR));
        label_3->setText(QApplication::translate("MainWindow", "Y Drift", Q_NULLPTR));
        sessionButton->setText(QApplication::translate("MainWindow", "Session Setup", Q_NULLPTR));
        xmlCheck->setText(QApplication::translate("MainWindow", "XML", Q_NULLPTR));
        jsonCheck->setText(QApplication::translate("MainWindow", "JSON", Q_NULLPTR));
        label_4->setText(QApplication::translate("MainWindow", "Export Format", Q_NULLPTR));
        reticleBox->setText(QApplication::translate("MainWindow", "Show Reticle", Q_NULLPTR));
        menuEngine_GUI->setTitle(QApplication::translate("MainWindow", "Engine GUI", Q_NULLPTR));
    } // retranslateUi

};

namespace Ui {
    class MainWindow: public Ui_MainWindow {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_MAINWINDOW_H
