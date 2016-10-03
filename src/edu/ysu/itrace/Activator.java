package edu.ysu.itrace;

import java.util.HashMap;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
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
    private IEditorPart activeEditor;
    // The shared instance
    private static Activator plugin;
    private HashMap<IEditorPart,TokenHighlighter> tokenHighlighters = new HashMap<IEditorPart,TokenHighlighter>();
    private boolean showTokenHighlights = false;
    
    /**
     * The constructor
     */
    public Activator() {
    	IEditorPart editorPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
    	tokenHighlighters.put(editorPart, new TokenHighlighter(editorPart,showTokenHighlights));
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
    
    public void setActiveEditor(IEditorPart editorPart){
    	activeEditor = editorPart;
    	if(!tokenHighlighters.containsKey(editorPart)) 
    		tokenHighlighters.put(editorPart, new TokenHighlighter(editorPart,showTokenHighlights));
    	
    }
    
    public void updateHighlighters(IEditorPart editorPart,Gaze gaze){
    	if(editorPart == null) editorPart = activeEditor;
    	tokenHighlighters.get(editorPart).updateHandleGaze(gaze);
    }
    
    public void removeHighlighter(IEditorPart editorPart){
    	tokenHighlighters.remove(editorPart);
    }
    
    public void showTokenHighLights(){
    	showTokenHighlights = !showTokenHighlights;
		if(!tokenHighlighters.containsKey(activeEditor)) 
    		tokenHighlighters.put(activeEditor, new TokenHighlighter(activeEditor, showTokenHighlights));
		if(activeEditor == null) return;
    	for(TokenHighlighter tokenHighlighter: tokenHighlighters.values()){
    		tokenHighlighter.setShow(showTokenHighlights);
    	}
    }

}
