package edu.ysu.itrace.solvers.emotionpopup;

import edu.ysu.itrace.gaze.IGazeResponse;
import edu.ysu.itrace.solvers.ISolver;

public interface IEmotionPopupHandler extends ISolver {
    void writeResponse(IGazeResponse response, String emotion, String[] options);
}