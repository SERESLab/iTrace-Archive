/********************************************************************************
** Form generated from reading UI file 'reticle.ui'
**
** Created by: Qt User Interface Compiler version 5.8.0
**
** WARNING! All changes made in this file will be lost when recompiling UI file!
********************************************************************************/

#ifndef UI_RETICLE_H
#define UI_RETICLE_H

#include <QtCore/QVariant>
#include <QtWidgets/QAction>
#include <QtWidgets/QApplication>
#include <QtWidgets/QButtonGroup>
#include <QtWidgets/QHeaderView>
#include <QtWidgets/QWidget>

QT_BEGIN_NAMESPACE

class Ui_Reticle
{
public:

    void setupUi(QWidget *Reticle)
    {
        if (Reticle->objectName().isEmpty())
            Reticle->setObjectName(QStringLiteral("Reticle"));
        Reticle->resize(24, 24);

        retranslateUi(Reticle);

        QMetaObject::connectSlotsByName(Reticle);
    } // setupUi

    void retranslateUi(QWidget *Reticle)
    {
        Reticle->setWindowTitle(QApplication::translate("Reticle", "Form", Q_NULLPTR));
    } // retranslateUi

};

namespace Ui {
    class Reticle: public Ui_Reticle {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_RETICLE_H
