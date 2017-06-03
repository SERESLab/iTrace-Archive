#ifndef TRACKER_H
#define TRACKER_H


class tracker
{
public:
    virtual void enterCalibration() = 0;
    virtual void leaveCalibration() = 0;
    virtual void useCalibrationPoint(float x, float y) = 0;
    virtual void discardCalibrationPoint(float x, float y) = 0;
};

#endif // TRACKER_H
