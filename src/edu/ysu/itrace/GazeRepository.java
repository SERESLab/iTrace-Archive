package edu.ysu.itrace;

import java.util.ArrayList;

public class GazeRepository {
    private static ArrayList<Gaze> gazes = new ArrayList<Gaze>();

    public static void addGaze(Gaze gaze) {
        gazes.add(gaze);
    }

    public static void removeGaze(Gaze gaze) {
        gazes.remove(gaze);
    }
}
