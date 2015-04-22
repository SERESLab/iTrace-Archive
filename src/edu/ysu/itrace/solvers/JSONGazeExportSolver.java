package edu.ysu.itrace.solvers;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.text.SimpleDateFormat;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

import com.google.gson.stream.JsonWriter;

import edu.ysu.itrace.gaze.IGazeResponse;

/**
 * Solver that simply dumps gaze data to disk in JSON format.
 */
public class JSONGazeExportSolver implements IFileExportSolver {
    private JsonWriter responseWriter;
    private File outFile;
    private String filenamePattern =
            "'gaze-responses-'yyyyMMdd'T'HHmmss','SSSSZ'.json'";
    private Dimension screenRect;
    private Shell parent;

    public JSONGazeExportSolver(Shell parent) {
        this.parent = parent;
    }

    @Override
    public String getFilenamePattern() {
        return filenamePattern;
    }

    @Override
    public void setFilenamePattern(String filenamePattern) {
        this.filenamePattern = filenamePattern;
    }

    @Override
    public void init() {
        screenRect = Toolkit.getDefaultToolkit().getScreenSize();
        try {
            outFile = new File(getFilename());

            // Check that file does not already exist. If it does, do not begin
            // tracking.
            if (outFile.exists()) {
                System.out.println(friendlyName());
                System.out.println("You cannot overwrite this file. If you "
                        + "wish to continue, delete the file " + "manually.");

                return;
            }

            responseWriter = new JsonWriter(new FileWriter(outFile));
            responseWriter.setIndent("  ");
        } catch (IOException e) {
            throw new RuntimeException("Log files could not be created: "
                    + e.getMessage());
        }
        System.out.println("Putting files at " + outFile.getAbsolutePath());

        try {
            responseWriter.beginObject()
                              .name("environment")
                              .beginObject()
                                  .name("screen_size")
                                  .beginObject()
                                      .name("width")
                                      .value(screenRect.width)
                                      .name("height")
                                      .value(screenRect.height)
                                  .endObject()
                              .endObject()
                              .name("gazes")
                              .beginArray();
        } catch (IOException e) {
            throw new RuntimeException("Log file header could not be written: "
                    + e.getMessage());
        }
    }

    @Override
    public void process(IGazeResponse response) {
        try {
                int screenX =
                        (int) (screenRect.width * response.getGaze().getX());
                int screenY =
                        (int) (screenRect.height * response.getGaze().getY());

                responseWriter.beginObject()
                                .name("file")
                                .value(response.getName())
                                .name("gaze-type")
                                .value(response.getGazeType())
                                .name("x")
                                .value(screenX)
                                .name("y")
                                .value(screenY)
                                .name("left_validation")
                                .value(response.getGaze().getLeftValidity())
                                .name("right_validation")
                                .value(response.getGaze().getRightValidity())
                                .name("left-pupil-diameter")
                                .value(response.getGaze().getLeftPupilDiameter())
                                .name("right-pupil-diameter")
                                .value(response.getGaze().getRightPupilDiameter())
                                .name("tracker_time")
                                .value(response.getGaze().getTrackerTime().getTime())
                                .name("system_time")
                                .value(response.getGaze().getSystemTime())
                                .name("nano_time")
                                .value(response.getGaze().getNanoTime())
                                .endObject();
        } catch (IOException e) {
            // ignore write errors
        }
    }

    @Override
    public void dispose() {
        try {
            responseWriter.endArray().endObject();
            responseWriter.flush();
            responseWriter.close();
            System.out.println("Gaze responses saved.");
        } catch (IOException e) {
            throw new RuntimeException("Log file footer could not be written: "
                    + e.getMessage());
        }
    }

    @Override
    public String getFilename() {
        String workspaceLocation =
                ResourcesPlugin.getWorkspace().getRoot().getLocation()
                        .toString();
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(filenamePattern);
            return workspaceLocation + "/" + formatter.format(new Date());
        } catch (IllegalArgumentException e) {
            return workspaceLocation + "/" + filenamePattern;
        }
    }

    @Override
    public String friendlyName() {
        return "JSON Gaze Export";
    }

    @Override
    public void config() {
        final InputDialog configDialog =
                new InputDialog(parent, friendlyName() + " Configuration",
                        "Export Filename Pattern", getFilenamePattern(), null);
        if (configDialog.open() == Window.OK) {
            setFilenamePattern(configDialog.getValue());
        }
    }
}
