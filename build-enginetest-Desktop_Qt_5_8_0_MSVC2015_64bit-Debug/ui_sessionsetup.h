/********************************************************************************
** Form generated from reading UI file 'sessionsetup.ui'
**
** Created by: Qt User Interface Compiler version 5.8.0
**
** WARNING! All changes made in this file will be lost when recompiling UI file!
********************************************************************************/

#ifndef UI_SESSIONSETUP_H
#define UI_SESSIONSETUP_H

#include <QtCore/QVariant>
#include <QtWidgets/QAction>
#include <QtWidgets/QApplication>
#include <QtWidgets/QButtonGroup>
#include <QtWidgets/QDialog>
#include <QtWidgets/QDialogButtonBox>
#include <QtWidgets/QHeaderView>
#include <QtWidgets/QLabel>
#include <QtWidgets/QPushButton>
#include <QtWidgets/QTextEdit>

QT_BEGIN_NAMESPACE

class Ui_sessionSetup
{
public:
    QDialogButtonBox *buttonBox;
    QLabel *label;
    QLabel *label_2;
    QLabel *label_3;
    QTextEdit *textEdit;
    QTextEdit *textEdit_2;
    QTextEdit *textEdit_3;
    QTextEdit *textEdit_4;
    QPushButton *pushButton;

    void setupUi(QDialog *sessionSetup)
    {
        if (sessionSetup->objectName().isEmpty())
            sessionSetup->setObjectName(QStringLiteral("sessionSetup"));
        sessionSetup->resize(300, 201);
        buttonBox = new QDialogButtonBox(sessionSetup);
        buttonBox->setObjectName(QStringLiteral("buttonBox"));
        buttonBox->setGeometry(QRect(-70, 160, 341, 32));
        buttonBox->setOrientation(Qt::Horizontal);
        buttonBox->setStandardButtons(QDialogButtonBox::Cancel|QDialogButtonBox::Ok);
        label = new QLabel(sessionSetup);
        label->setObjectName(QStringLiteral("label"));
        label->setGeometry(QRect(20, 20, 61, 21));
        label_2 = new QLabel(sessionSetup);
        label_2->setObjectName(QStringLiteral("label_2"));
        label_2->setGeometry(QRect(20, 50, 91, 16));
        label_3 = new QLabel(sessionSetup);
        label_3->setObjectName(QStringLiteral("label_3"));
        label_3->setGeometry(QRect(20, 80, 91, 16));
        textEdit = new QTextEdit(sessionSetup);
        textEdit->setObjectName(QStringLiteral("textEdit"));
        textEdit->setGeometry(QRect(120, 20, 151, 21));
        textEdit->setVerticalScrollBarPolicy(Qt::ScrollBarAlwaysOff);
        textEdit_2 = new QTextEdit(sessionSetup);
        textEdit_2->setObjectName(QStringLiteral("textEdit_2"));
        textEdit_2->setGeometry(QRect(120, 50, 151, 21));
        textEdit_2->setVerticalScrollBarPolicy(Qt::ScrollBarAlwaysOff);
        textEdit_3 = new QTextEdit(sessionSetup);
        textEdit_3->setObjectName(QStringLiteral("textEdit_3"));
        textEdit_3->setGeometry(QRect(120, 80, 151, 21));
        textEdit_3->setVerticalScrollBarPolicy(Qt::ScrollBarAlwaysOff);
        textEdit_4 = new QTextEdit(sessionSetup);
        textEdit_4->setObjectName(QStringLiteral("textEdit_4"));
        textEdit_4->setGeometry(QRect(120, 110, 151, 21));
        textEdit_4->setVerticalScrollBarPolicy(Qt::ScrollBarAlwaysOff);
        pushButton = new QPushButton(sessionSetup);
        pushButton->setObjectName(QStringLiteral("pushButton"));
        pushButton->setGeometry(QRect(20, 110, 81, 23));

        retranslateUi(sessionSetup);
        QObject::connect(buttonBox, SIGNAL(accepted()), sessionSetup, SLOT(accept()));
        QObject::connect(buttonBox, SIGNAL(rejected()), sessionSetup, SLOT(reject()));

        QMetaObject::connectSlotsByName(sessionSetup);
    } // setupUi

    void retranslateUi(QDialog *sessionSetup)
    {
        sessionSetup->setWindowTitle(QApplication::translate("sessionSetup", "Dialog", Q_NULLPTR));
        label->setText(QApplication::translate("sessionSetup", "Study Name", Q_NULLPTR));
        label_2->setText(QApplication::translate("sessionSetup", "Researcher Name", Q_NULLPTR));
        label_3->setText(QApplication::translate("sessionSetup", "Participant ID", Q_NULLPTR));
        pushButton->setText(QApplication::translate("sessionSetup", "Data Directory", Q_NULLPTR));
    } // retranslateUi

};

namespace Ui {
    class sessionSetup: public Ui_sessionSetup {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_SESSIONSETUP_H
