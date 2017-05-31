#-------------------------------------------------
#
# Project created by QtCreator 2017-01-25T21:42:14
#
#-------------------------------------------------

QT       += core gui

greaterThan(QT_MAJOR_VERSION, 4): QT += widgets

TARGET = enginetest
TEMPLATE = app

# The following define makes your compiler emit warnings if you use
# any feature of Qt which as been marked as deprecated (the exact warnings
# depend on your compiler). Please consult the documentation of the
# deprecated API in order to know how to port your code away from it.
DEFINES += QT_DEPRECATED_WARNINGS

# You can also make your code fail to compile if you use deprecated APIs.
# In order to do so, uncomment the following line.
# You can also select to disable deprecated APIs only up to a certain version of Qt.
#DEFINES += QT_DISABLE_DEPRECATED_BEFORE=0x060000    # disables all the APIs deprecated before Qt 6.0.0

QT += network


SOURCES += main.cpp\
        mainwindow.cpp \
    mousetracker.cpp \
<<<<<<< Updated upstream
    tobiipro.cpp \
    calibrationscreen.cpp

HEADERS  += mainwindow.h \
    mousetracker.h \
    tobiifunctions.h \
    tobiipro.h \
    calibrationscreen.h
=======
    sessionsetup.cpp

HEADERS  += mainwindow.h \
    mousetracker.h \
    sessionsetup.h
>>>>>>> Stashed changes

FORMS    += mainwindow.ui \
    sessionsetup.ui

<<<<<<< Updated upstream


win32: LIBS += -L$$PWD/../../../TobiiPro/TobiiPro/64/lib/ -ltobii_research

INCLUDEPATH += $$PWD/../../../TobiiPro/TobiiPro/64/include
DEPENDPATH += $$PWD/../../../TobiiPro/TobiiPro/64/include
=======
>>>>>>> Stashed changes
