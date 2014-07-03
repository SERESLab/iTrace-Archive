package edu.ysu.itrace.solvers;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map.Entry;
import java.text.SimpleDateFormat;

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
            responseWriter = outFactory.createXMLStreamWriter(outFile);
        } catch (IOException | XMLStreamException e) {
            throw new RuntimeException("Log files could not be created: " +
                    e.getMessage());
        }
        System.out.println("Putting files at " + currentFilename);

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
            responseWriter.writeStartElement("line-height");
            responseWriter.writeCharacters(String.valueOf(lineHeight));
            responseWriter.writeEndElement();
            responseWriter.writeCharacters(EOL);
            responseWriter.writeStartElement("font-height");
            responseWriter.writeCharacters(String.valueOf(fontHeight));
            responseWriter.writeEndElement();
            responseWriter.writeCharacters(EOL);
            responseWriter.writeEndElement();
            responseWriter.writeCharacters(EOL);
            responseWriter.writeStartElement("gazes");
            responseWriter.writeCharacters(EOL);
        } catch (Exception e) {
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

                responseWriter.writeEmptyElement("response");
                responseWriter.writeAttribute("file",
                        response.getName());
                responseWriter.writeAttribute("type",
                        response.getType());
                responseWriter.writeAttribute("x",
                        String.valueOf(screenX));
                responseWriter.writeAttribute("y",
                        String.valueOf(screenY));
                responseWriter.writeAttribute("left-validation",
                        String.valueOf(response.getGaze()
                        .getLeftValidity()));
                responseWriter.writeAttribute("right-validation",
                        String.valueOf(response.getGaze()
                        .getRightValidity()));
                responseWriter.writeAttribute("timestamp",
                        String.valueOf(response.getGaze()
                        .getTimeStamp().getTime()));

                for(Iterator<Entry<String,String>> entries
                    = response.getProperties().entrySet()
                    .iterator();
                        entries.hasNext(); ){
                    Entry<String,String> pair = entries.next();
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
            outFile.close();
            System.out.println("Gaze responses saved.");
        } catch (XMLStreamException | IOException e) {
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
