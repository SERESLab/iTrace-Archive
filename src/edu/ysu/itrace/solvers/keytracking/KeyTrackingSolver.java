package edu.ysu.itrace.solvers.keytracking;

import edu.ysu.itrace.gaze.IGazeResponse;

import java.awt.Dimension;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Listener;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import com.google.gson.stream.JsonWriter;

public class KeyTrackingSolver implements IKeyTrackingSolver, EventHandler {
    private JsonWriter responseWriter;
    private File outFile;
    private String filename = "key-responses-USERNAME-yyMMddTHHmmss-SSSS-Z.json";
    private String sessionID;
	private IGazeResponse lastGazeResponse;
	private Listener swtListener;

    public KeyTrackingSolver() {
    	UIManager.put("swing.boldMetal", new Boolean(false)); //make UI font plain
    }

    @Override
    public void init() {
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

            //responseWriter.setIndent("");
            // to pretty print, use this one instead
            responseWriter.setIndent("  ");
        } catch (IOException e) {
            throw new RuntimeException("Log files could not be created: "
                    + e.getMessage());
        }
        System.out.println("Putting files at " + outFile.getAbsolutePath());

        try {
            responseWriter.beginObject()
                          .name("responses")
                          .beginArray();
        } catch (IOException e) {
            throw new RuntimeException("Log file header could not be written: "
                    + e.getMessage());
        }
        
        this.swtListener = new Listener() {
			@Override
			public void handleEvent(org.eclipse.swt.widgets.Event event) {
                writeResponse(lastGazeResponse, event);
			}
        };
        
        Display.getDefault().addFilter(SWT.KeyDown, swtListener);
    }

    @Override
    public void dispose() {
        Display.getDefault().removeFilter(SWT.KeyDown, swtListener);
        
        try {
            responseWriter.endArray()
                          .endObject();
            responseWriter.flush();
            responseWriter.close();
            System.out.println("Key responses saved.");
        } catch (IOException e) {
            throw new RuntimeException("Log file footer could not be written: "
                    + e.getMessage());
        }
        outFile = null;
    }

    @Override
    public void config(String sessionID, String devUsername) {
    	filename = "key-responses-" + devUsername +
    			"-" + sessionID + ".json";
    	this.sessionID = sessionID;
    }
    
    public String getFilename() {
        String workspaceLocation =
                ResourcesPlugin.getWorkspace().getRoot().getLocation()
                        .toString();
        return workspaceLocation + "/" + sessionID + "/" + filename;
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

	public void handleEvent(Event event) {
		String[] propertyNames = event.getPropertyNames();
		IGazeResponse response = (IGazeResponse)event.getProperty(propertyNames[0]);
		this.lastGazeResponse = response;
	}

    public String friendlyName() {
        return "Key Tracking";
    }

    private void writeResponse(IGazeResponse response, org.eclipse.swt.widgets.Event event) {
        try {
                responseWriter.beginObject();
                
                if(this.lastGazeResponse != null)
                {
                	responseWriter.name("session_time")
				                  .value(response.getGaze().getSessionTime())
				                  .name("tracker_time")
				                  .value(response.getGaze().getTrackerTime())
				                  .name("system_time")
				                  .value(response.getGaze().getSystemTime());
                }
                
                if(event.item != null)
                {
                	responseWriter.name("item")
                				  .value(event.item.toString());
                }
                
                if(event.widget != null)
                {
                	responseWriter.name("widget")
                				  .value(event.widget.toString());
                }

                responseWriter.name("string")
                              .value(String.valueOf(event.character))
                              .name("text")
                              .value(event.text)
                              .name("character")
                              .value(event.character)
                              .name("keyCode")
                              .value(event.keyCode)
                              .name("keyLocation")
                              .value(event.keyLocation)
                              .name("start")
                              .value(event.start)
                              .name("end")
                              .value(event.end)
                              .name("allowed")
                              .value(event.doit)
                              .endObject();
        } catch (IOException e) {
            // ignore write errors
        }
    }

	@Override
	public void process(IGazeResponse response) {
		// Do nothing
	}
}
