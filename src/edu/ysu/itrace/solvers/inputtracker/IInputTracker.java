package edu.ysu.itrace.solvers.inputtracker;

import edu.ysu.itrace.gaze.IGazeResponse;
import edu.ysu.itrace.solvers.ISolver;

public interface IInputTracker extends ISolver {
    void writeResponse(IGazeResponse response, String emotion, String[] options);
}