package edu.ysu.itrace.solvers;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;
import java.text.SimpleDateFormat;

import org.eclipse.core.resources.ResourcesPlugin;

import com.google.gson.stream.JsonWriter;

import edu.ysu.itrace.gaze.IGazeResponse;

/**
 * Solver that simply dumps gaze data to disk in JSON format.
 */
public class JSONGazeExportSolver implements IFileExportSolver {
    private JsonWriter responseWriter;
    private FileWriter outFile;
    private String filenamePattern;
    private Dimension screenRect;
    private int lineHeight;
    private int fontHeight;

    @Override
    public String getFilenamePattern() {
        return filenamePattern;
    }

    @Override
    public void setFilenamePattern(String filenamePattern) {
        this.filenamePattern = filenamePattern;
    }

    public int getLineHeight() {
        return lineHeight;
    }

    public void setLineHeight(int lineHeight) {
        this.lineHeight = lineHeight;
    }

    public int getFontHeight() {
        return fontHeight;
    }

    public void setFontHeight(int fontHeight) {
        this.fontHeight = fontHeight;
    }

    @Override
    public void init() {
        screenRect = Toolkit.getDefaultToolkit().getScreenSize();
        String currentFilename;
        try {
            currentFilename = getFilename();
            outFile = new FileWriter(currentFilename);
            responseWriter = new JsonWriter(outFile);
            responseWriter.setIndent("  ");
        } catch (IOException e) {
            throw new RuntimeException("Log files could not be created: " +
                    e.getMessage());
        }
        System.out.println("Putting files at " + currentFilename);

        try {
            responseWriter.beginObject()
                              .name("environment")
                              .beginObject()
                                  .name("screen-size")
                                  .beginObject()
                                      .name("width")
                                      .value(screenRect.width)
                                      .name("height")
                                      .value(screenRect.height)
                                  .endObject()
                                  .name("line-height")
                                  .value(lineHeight)
                                  .name("font-height")
                                  .value(fontHeight)
                              .endObject()
                              .name("gazes")
                              .beginArray();
        } catch (IOException e) {
            throw new RuntimeException("Log file header could not be written: " +
                                       e.getMessage());
        }
    }

    @Override
    public void process(IGazeResponse response) {
        try {
            if(response.getProperties().size() > 0){
                int screenX = (int) (screenRect.width * response
                              .getGaze().getX());
                int screenY = (int) (screenRect.height * response
                              .getGaze().getY());

                responseWriter.beginObject()
                                  .name("file")
                                  .value(response.getName())
                                  .name("type")
                                  .value(response.getType())
                                  .name("x")
                                  .value(screenX)
                                  .name("y")
                                  .value(screenY)
                                  .name("left-validation")
                                  .value(response.getGaze().getLeftValidity())
                                  .name("right-validation")
                                  .value(response.getGaze().getRightValidity())
                                  .name("timestamp")
                                  .value(response.getGaze().getTimeStamp().getTime());

                for(Iterator<Entry<String,String>> entries
                    = response.getProperties().entrySet().iterator();
                        entries.hasNext(); ){
                    Entry<String,String> pair = entries.next();
                    responseWriter.name(pair.getKey())
                                  .value(pair.getValue());
                }

                responseWriter.endObject();
            }
        } catch (IOException e) {
            // ignore write errors
        }
    }

    @Override
    public void dispose() {
        try {
            responseWriter.endArray()
                          .endObject();
            responseWriter.flush();
            responseWriter.close();
            System.out.println("Gaze responses saved.");
        } catch (IOException e) {
            throw new RuntimeException("Log file footer could not be written: " +
                    e.getMessage());
        }
    }

    @Override
    public String getFilename() {
        String workspaceLocation = ResourcesPlugin.getWorkspace().getRoot()
                .getLocation().toString();
        SimpleDateFormat formatter = new SimpleDateFormat(filenamePattern);
        return workspaceLocation + "/" + formatter.format(new Date());
    }
}
