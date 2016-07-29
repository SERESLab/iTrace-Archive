package edu.ysu.itrace;

import java.util.HashMap;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import edu.ysu.itrace.preferences.PluginPreferences;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "edu.ysu.itrace"; //$NON-NLS-1$
    public long sessionStartTime;
    public GazeTransport gazeTransport;
    public Rectangle monitorBounds;
    public IEditorPart activeEditor;
    // The shared instance
    private static Activator plugin;
    private HashMap<IEditorPart,TokenHighlighter> tokenHighlighters = new HashMap<IEditorPart,TokenHighlighter>();
    
    
    /**
     * The constructor
     */
    public Activator() {
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        IPreferenceStore prefStore = getDefault().getPreferenceStore();
        EyeTrackerFactory.setTrackerType(EyeTrackerFactory.TrackerType.valueOf(
                prefStore.getString(PluginPreferences.EYE_TRACKER_TYPE)));
        activeEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        plugin = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }
    
    public void updateHighlighters(IEditorPart editorPart, int line, int column){
    	if(editorPart == null){
    		editorPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
    		if(editorPart == null) return;
    	}
    	if(!tokenHighlighters.containsKey(editorPart)) 
    		tokenHighlighters.put(editorPart, new TokenHighlighter(editorPart));
    	tokenHighlighters.get(editorPart).update(line,column);
    }
    
    public void updateHighlighters(IEditorPart editorPart,Gaze gaze){
    	if(editorPart == null) editorPart = activeEditor;
    	//System.out.println("asdf");
    	if(!tokenHighlighters.containsKey(editorPart)) 
    		tokenHighlighters.put(editorPart, new TokenHighlighter(editorPart));
    	tokenHighlighters.get(editorPart).updateHandleGaze(gaze);
    }
    
    public void showTokenHighLights(){
		if(!tokenHighlighters.containsKey(activeEditor)) 
    		tokenHighlighters.put(activeEditor, new TokenHighlighter(activeEditor));
		if(activeEditor == null) return;
    	for(TokenHighlighter tokenHighlighter: tokenHighlighters.values()){
    		tokenHighlighter.setShow();
    	}
    }

}
