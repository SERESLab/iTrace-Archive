package edu.ysu.itrace.solvers;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

import edu.ysu.itrace.gaze.IGazeResponse;

public class CSVGazeExportSolver implements IFileExportSolver {

	private static final String SEPARATOR = ",";
	private static final String NEW_LINE =  System.getProperty("line.separator");
	
	private Shell parent;
	private File outFile;
	private FileWriter responseWriter;
	private String fileNamePattern = "'gaze-responses-'yyyyMMdd'T'HHmmss','SSSSZ'.csv'";
	private Dimension screenRect;
 
	public CSVGazeExportSolver(Shell parent) {
		this.parent = parent;
	}

	@Override
	public String friendlyName() {
		 return "CSV Gaze Export";
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

	@Override
	public void init() {
	    try {
	    	screenRect = Toolkit.getDefaultToolkit().getScreenSize();
	    	outFile = new File(getFilename());

	            // Check that file does not already exist. If it does, do not begin
	            // tracking.
	            if (outFile.exists()) {
	                System.out.println(friendlyName());
	                System.out.println("You cannot overwrite this file. If you "
	                        + "wish to continue, delete the file " + "manually.");
	                return;
	            }

	            responseWriter = new FileWriter(outFile);
	        } catch (IOException e) {
	            throw new RuntimeException("Log files could not be created: "
	                    + e.getMessage());
	        }
	        System.out.println("Putting files at " + outFile.getAbsolutePath());

	        try {
	        	StringBuilder output = new StringBuilder();
	        	output.append("screen dimension");
	        	output.append(SEPARATOR);
	        	output.append("file");
	        	output.append(SEPARATOR);
	        	output.append("type");
	        	output.append(SEPARATOR);
	        	output.append("x");
	        	output.append(SEPARATOR);
	        	output.append("y");
	        	output.append(SEPARATOR);
	        	output.append("left validation");
	        	output.append(SEPARATOR);
	        	output.append("right validation");
	        	output.append(SEPARATOR);
	        	output.append("left pupil diameter");
	        	output.append(SEPARATOR);
	        	output.append("right pupil diameter");
	        	output.append(SEPARATOR);
	        	output.append("fixation");
	        	output.append(SEPARATOR);
	        	output.append("tracker time");
	        	output.append(SEPARATOR);
	        	output.append("system time");
	        	output.append(SEPARATOR);
	        	output.append("nano time");
	        	output.append(SEPARATOR);
	        	output.append("line");
	        	output.append(SEPARATOR);
	        	output.append("column");
	        	output.append(SEPARATOR);
	        	output.append("fully qualified name");
	        	output.append(NEW_LINE);
	        	responseWriter.write(output.toString());
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
			StringBuilder output = new StringBuilder();
			output.append(screenX + "/" + screenY);
			output.append(SEPARATOR);
			output.append(response.getName());
			output.append(SEPARATOR);
			output.append(response.getType());
			output.append(SEPARATOR);
			output.append(screenX);
			output.append(SEPARATOR);
			output.append(screenY);
			output.append(SEPARATOR);
			output.append(response.getGaze().getLeftValidity());
			output.append(SEPARATOR);
			output.append(response.getGaze().getRightValidity());
			output.append(SEPARATOR);
			output.append(response.getGaze().getLeftPupilDiameter());
			output.append(SEPARATOR);
			output.append(response.getGaze().getRightPupilDiameter());
			output.append(SEPARATOR);
	        output.append(response.getGaze().isFixation());
	        output.append(SEPARATOR);
	        output.append(response.getGaze().getTrackerTime());
	        output.append(SEPARATOR);
	        output.append(response.getGaze().getSystemTime());
	        output.append(SEPARATOR);
	        output.append(response.getGaze().getNanoTime());
	        output.append(SEPARATOR);
	        output.append(response.getProperties().get("line"));
	        output.append(SEPARATOR);
	        output.append(response.getProperties().get("col"));
	        output.append(SEPARATOR);
	        output.append(response.getProperties().get("fullyQualifiedName"));
	        output.append(NEW_LINE);
	        responseWriter.write(output.toString());
        } catch (IOException e) {
            // ignore write errors
        }
	}

	@Override
	public void dispose() {
		 try {
            responseWriter.flush();
	        responseWriter.close();
	        System.out.println("Gaze responses saved.");
        } catch (IOException e) {
            throw new RuntimeException("Log file footer could not be written: "
                    + e.getMessage());
        }
	}

	@Override
	public String getFilenamePattern() {
		return fileNamePattern;
	}

	@Override
	public void setFilenamePattern(String filenamePattern) {
		this.fileNamePattern = filenamePattern;
	}	
	
	@Override
	public String getFilename() {
		String workspaceLocation =
                ResourcesPlugin.getWorkspace().getRoot().getLocation()
                        .toString();
        try {
            SimpleDateFormat formatter = new SimpleDateFormat(fileNamePattern);
            return workspaceLocation + "/" + formatter.format(new Date());
        } catch (IllegalArgumentException e) {
            return workspaceLocation + "/" + fileNamePattern;
        }
	}

}