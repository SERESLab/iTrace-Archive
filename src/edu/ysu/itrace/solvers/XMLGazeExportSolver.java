package edu.ysu.itrace.solvers;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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

import edu.ysu.itrace.AstManager.SourceCodeEntity;
import edu.ysu.itrace.SOManager.StackOverflowEntity;
import edu.ysu.itrace.BRManager.BugReportEntity;
import edu.ysu.itrace.gaze.IGazeResponse;
import edu.ysu.itrace.gaze.IStackOverflowGazeResponse;
import edu.ysu.itrace.gaze.IBugReportGazeResponse;
import edu.ysu.itrace.gaze.IStyledTextGazeResponse;

/**
 * Solver that simply dumps gaze data to disk in XML format.
 */
public class XMLGazeExportSolver implements IFileExportSolver {
    private static final String EOL = System.getProperty("line.separator");
    private XMLOutputFactory outFactory = XMLOutputFactory.newInstance();
    private XMLStreamWriter responseWriter;
    private File outFile;
    private String filename = "gaze-responses-USERNAME"
    		+ "-yyMMddTHHmmss-SSSS-Z.xml";
    private Dimension screenRect;
    private String sessionID;

    public XMLGazeExportSolver() {
    	UIManager.put("swing.boldMetal", new Boolean(false)); //make UI font plain
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
                        "timestamp",
                        String.valueOf(response.getGaze().getTimestamp()));
                responseWriter.writeAttribute(
                        "session_time",
                        String.valueOf(response.getGaze().getSessionTime()));
                responseWriter.writeAttribute(
                        "system/tracker/nano",
                        response.getGaze().getSystemTime()+"/"+
                        response.getGaze().getTrackerTime()+"/"+
                        response.getGaze().getNanoTime());

                if (response instanceof IStyledTextGazeResponse) {
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

                } else if (response instanceof IStackOverflowGazeResponse) {
                	IStackOverflowGazeResponse stackOverflowResponse =
                            (IStackOverflowGazeResponse) response;
                	StackOverflowEntity soe = stackOverflowResponse.getSOE();
                    responseWriter.writeAttribute("url", stackOverflowResponse.getURL());
                    responseWriter.writeAttribute("Id", stackOverflowResponse.getID());
                    responseWriter.writeStartElement("soe");
                    responseWriter.writeAttribute("part", soe.part.toString());
                    responseWriter.writeAttribute("part_number", String.valueOf(soe.partNum));
                    responseWriter.writeAttribute("type", soe.type.toString());
                    responseWriter.writeAttribute("type_number", String.valueOf(soe.typeNum));
                    responseWriter.writeEndElement();
                } 
                else if (response instanceof IBugReportGazeResponse) {
                	IBugReportGazeResponse bugReportResponse =
                            (IBugReportGazeResponse) response;
                	BugReportEntity bre = bugReportResponse.getBRE();
                    responseWriter.writeAttribute("url", bugReportResponse.getURL());
                    responseWriter.writeAttribute("Id", bugReportResponse.getID());
                    responseWriter.writeStartElement("bre");
                    responseWriter.writeAttribute("part", bre.part.toString());
                    responseWriter.writeAttribute("part_number", String.valueOf(bre.partNum));
                    responseWriter.writeAttribute("type", bre.type.toString());
                    responseWriter.writeAttribute("type_number", String.valueOf(bre.typeNum));
                    responseWriter.writeEndElement();
                }
                
                else {
                	//ignore anything else
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
    public void config(String sessionID, String devUsername) {
    	filename = "gaze-responses-" + devUsername + "-"
    			+ sessionID + ".xml";
    	this.sessionID = sessionID;
    }

    @Override
    public String getFilename() {
        String workspaceLocation =
                ResourcesPlugin.getWorkspace().getRoot().getLocation()
                        .toString();
        return workspaceLocation + "/" + sessionID + "/" + filename;
    }

    @Override
    public String friendlyName() {
        return "XML Gaze Export";
    }

    @Override
    public void displayExportFile() {
    	JTextField displayVal = new JTextField(filename);
    	displayVal.setEditable(false);
    	
    	JPanel displayPanel = new JPanel();
    	displayPanel.setLayout(new BoxLayout(displayPanel, BoxLayout.Y_AXIS)); //vertically align
    	displayPanel.add(new JLabel("Export Filename"));
    	displayPanel.add(displayVal);
    	displayPanel.setPreferredSize(new Dimension(400,40)); //resize appropriately
    	
    	final int displayDialog = JOptionPane.showConfirmDialog(null, displayPanel, 
    			friendlyName() + " Display", JOptionPane.OK_CANCEL_OPTION,
    			JOptionPane.PLAIN_MESSAGE);
    	if (displayDialog == JOptionPane.OK_OPTION) {
    		//do nothing
    	}
    }
}
