package edu.ysu.itrace.solvers;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.text.SimpleDateFormat;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

import edu.ysu.itrace.AstManager.SourceCodeEntity;
import edu.ysu.itrace.gaze.IGazeResponse;
import edu.ysu.itrace.gaze.IStyledTextGazeResponse;

/**
 * Solver that simply dumps gaze data to disk in XML format.
 */
public class XMLGazeExportSolver implements IFileExportSolver {
    private static final String EOL = System.getProperty("line.separator");
    private XMLOutputFactory outFactory = XMLOutputFactory.newInstance();
    private XMLStreamWriter responseWriter;
    private File outFile;
    private String filenamePattern =
            "'gaze-responses-'yyyyMMdd'T'HHmmss','SSSSZ'.xml'";
    private Dimension screenRect;
    private Shell parent;

    public XMLGazeExportSolver(Shell parent) {
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

            responseWriter =
                    outFactory.createXMLStreamWriter(new FileOutputStream(outFile), "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException("Log files could not be created: "
                    + e.getMessage());
        } catch (XMLStreamException e) {
            throw new RuntimeException("Log files could not be created: "
                    + e.getMessage());
        }
        System.out.println("Putting files at " + outFile.getAbsolutePath());

        try {
            responseWriter.writeStartDocument("utf-8", "1.0");
            responseWriter.writeCharacters(EOL);
            responseWriter.writeStartElement("itrace-records");
            responseWriter.writeCharacters(EOL);
            responseWriter.writeStartElement("environment");
            responseWriter.writeCharacters(EOL);
            responseWriter.writeEmptyElement("screen-size");
            responseWriter.writeAttribute("width",
                    String.valueOf(screenRect.width));
            responseWriter.writeAttribute("height",
                    String.valueOf(screenRect.height));
            responseWriter.writeCharacters(EOL);
            responseWriter.writeEndElement();
            responseWriter.writeCharacters(EOL);
            responseWriter.writeStartElement("gazes");
            responseWriter.writeCharacters(EOL);
        } catch (Exception e) {
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

                responseWriter.writeStartElement("response");
                responseWriter.writeAttribute("name", response.getName());
                responseWriter.writeAttribute("type", response.getGazeType());
                responseWriter.writeAttribute("x", String.valueOf(screenX));
                responseWriter.writeAttribute("y", String.valueOf(screenY));
                responseWriter.writeAttribute("left_validation",
                        String.valueOf(response.getGaze().getLeftValidity()));
                responseWriter.writeAttribute("right_validation",
                        String.valueOf(response.getGaze().getRightValidity()));
                responseWriter.writeAttribute("left_pupil_diameter",
                        String.valueOf(response.getGaze()
                                       .getLeftPupilDiameter()));
                responseWriter.writeAttribute("right_pupil_diameter",
                        String.valueOf(response.getGaze()
                                       .getRightPupilDiameter()));
                responseWriter.writeAttribute(
                        "tracker_time",
                        String.valueOf(response.getGaze().getTrackerTime()
                                .getTime()));
                responseWriter.writeAttribute(
                        "system_time",
                        String.valueOf(response.getGaze().getSystemTime()));
                responseWriter.writeAttribute(
                        "nano_time",
                        String.valueOf(response.getGaze().getNanoTime()));

                try {
                    IStyledTextGazeResponse styledResponse =
                            (IStyledTextGazeResponse) response;
                    responseWriter.writeAttribute("path", styledResponse.getPath());
                    responseWriter.writeAttribute("line_height",
                            String.valueOf(styledResponse.getLineHeight()));
                    responseWriter.writeAttribute("font_height",
                            String.valueOf(styledResponse.getFontHeight()));
                    responseWriter.writeAttribute("line",
                            String.valueOf(styledResponse.getLine()));
                    responseWriter.writeAttribute("col",
                            String.valueOf(styledResponse.getCol()));
                    responseWriter.writeAttribute("line_base_x",
                            String.valueOf(styledResponse.getLineBaseX()));
                    responseWriter.writeAttribute("line_base_y",
                            String.valueOf(styledResponse.getLineBaseY()));
                    responseWriter.writeStartElement("sces");
                    for (SourceCodeEntity sce : styledResponse.getSCEs()) {
                        responseWriter.writeStartElement("sce");
                        responseWriter.writeAttribute("name", sce.name);
                        responseWriter.writeAttribute("type", sce.type.toString());
                        responseWriter.writeAttribute("how", sce.how.toString());
                        responseWriter.writeAttribute("total_length",
                                String.valueOf(sce.totalLength));
                        responseWriter.writeAttribute("start_line",
                                String.valueOf(sce.startLine));
                        responseWriter.writeAttribute("end_line",
                                String.valueOf(sce.endLine));
                        responseWriter.writeAttribute("start_col",
                                String.valueOf(sce.startCol));
                        responseWriter.writeAttribute("end_col",
                                String.valueOf(sce.endCol));
                        responseWriter.writeEndElement();
                    }
                    responseWriter.writeEndElement();

                } catch (ClassCastException e) {
                    // not styled text, oh well
                }
                responseWriter.writeEndElement();
                responseWriter.writeCharacters(EOL);
        } catch (XMLStreamException e) {
            // ignore write errors
        }
    }

    @Override
    public void dispose() {
        try {
            responseWriter.writeEndElement();
            responseWriter.writeCharacters(EOL);
            responseWriter.writeEndElement();
            responseWriter.writeCharacters(EOL);
            responseWriter.writeEndDocument();
            responseWriter.writeCharacters(EOL);
            responseWriter.flush();
            responseWriter.close();
            System.out.println("Gaze responses saved.");
        } catch (XMLStreamException e) {
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
        return "XML Gaze Export";
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
