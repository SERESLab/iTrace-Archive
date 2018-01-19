package edu.ysu.itrace.solvers.externallauncher;

import edu.ysu.itrace.gaze.IGazeResponse;

import java.io.IOException;

import org.eclipse.core.resources.ResourcesPlugin;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

public class ExternalLauncher implements IExternalLauncher, EventHandler {
    private String filenameSuffix = "-responses-USERNAME-yyMMddTHHmmss-SSSS-Z.csv";
	private String sessionID;
    
    @Override
    public void config(String sessionID, String devUsername) {
    	filenameSuffix = "-responses-" + devUsername + "-" + sessionID + ".csv";
    	this.sessionID = sessionID;
    }
    
    public String getFilename(String prefix) {
        String workspaceLocation = ResourcesPlugin.getWorkspace().getRoot().getLocation().toString();
        return workspaceLocation + "/" + sessionID + "/" + prefix + filenameSuffix;
    }
    
    public void displayExportFile() {
    	// Do nothing
        // TODO: Potentially split this into two classes so that we can support this?
    }
    
    public void dispose() {
        // Do nothing
    }

    public String friendlyName() {
        return "External Application Launcher";
    }
    
    public void init() {
        this.start();
    }
    
    public void process(IGazeResponse response) {
        // Do nothing
    }
    
	public void handleEvent(Event event) {
		// Do nothing
	}
    
    public void start() {
        System.out.println("Launching external applications.");
        	
        // TODO: Make these paths configurable.
        
        try {
			new ProcessBuilder("C:\\Files\\AffectivaGSRFinal\\iTrace-Archive-SessionTimeServ\\iTraceAffectiva.exe", getFilename("affectiva")).start();
		} catch(IOException e) {
			e.printStackTrace();
		}
        
        try {
	        new ProcessBuilder("C:\\Files\\AffectivaGSRFinal\\iTrace-Archive-SessionTimeServ\\iTraceShimmerCapture.exe", getFilename("shimmer")).start();
		} catch(IOException e) {
			e.printStackTrace();
		}
    }
}
