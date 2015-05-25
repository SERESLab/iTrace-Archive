package edu.ysu.itrace.solvers;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;
import java.text.SimpleDateFormat;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.eclipse.core.resources.ResourcesPlugin;

import edu.ysu.itrace.gaze.IGazeResponse;

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

    public XMLGazeExportSolver() {
    	UIManager.put("swing.boldMetal", new Boolean(false)); //make UI font plain
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
            if (response.getProperties().size() > 0) {
                int screenX =
                        (int) (screenRect.width * response.getGaze().getX());
                int screenY =
                        (int) (screenRect.height * response.getGaze().getY());

                responseWriter.writeEmptyElement("response");
                responseWriter.writeAttribute("file", response.getName());
                responseWriter.writeAttribute("type", response.getType());
                responseWriter.writeAttribute("x", String.valueOf(screenX));
                responseWriter.writeAttribute("y", String.valueOf(screenY));
                responseWriter.writeAttribute("left-validation",
                        String.valueOf(response.getGaze().getLeftValidity()));
                responseWriter.writeAttribute("right-validation",
                        String.valueOf(response.getGaze().getRightValidity()));
                responseWriter.writeAttribute("left-pupil-diameter",
                        String.valueOf(response.getGaze()
                                       .getLeftPupilDiameter()));
                responseWriter.writeAttribute("right-pupil-diameter",
                        String.valueOf(response.getGaze()
                                       .getRightPupilDiameter()));
                responseWriter.writeAttribute(
                        "tracker-time",
                        String.valueOf(response.getGaze().getTrackerTime()
                                .getTime()));
                responseWriter.writeAttribute(
                        "system-time",
                        String.valueOf(response.getGaze().getSystemTime()));
                responseWriter.writeAttribute(
                        "nano-time",
                        String.valueOf(response.getGaze().getNanoTime()));

                for (Iterator<Entry<String, String>> entries =
                        response.getProperties().entrySet().iterator(); entries
                        .hasNext();) {
                    Entry<String, String> pair = entries.next();
                    responseWriter.writeAttribute(pair.getKey(),
                            pair.getValue());
                }
                responseWriter.writeCharacters(EOL);
            }
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
    	JTextField configVal = new JTextField(getFilenamePattern());
    	
    	JPanel configPanel = new JPanel();
    	configPanel.setLayout(new BoxLayout(configPanel, BoxLayout.Y_AXIS)); //vertically align
        configPanel.add(new JLabel("Export Filename Pattern"));
        configPanel.add(configVal);
        configPanel.setPreferredSize(new Dimension(300,40)); //resize appropriately

        final int configDialog = JOptionPane.showConfirmDialog(null, configPanel, 
                 friendlyName() + " Configuration", JOptionPane.OK_CANCEL_OPTION,
                 JOptionPane.PLAIN_MESSAGE);
        if (configDialog == JOptionPane.OK_OPTION) {
           setFilenamePattern(configVal.getText());
        }
    }
}
