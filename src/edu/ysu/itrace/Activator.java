package edu.ysu.itrace;

import java.io.File;
import java.util.HashMap;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import edu.ysu.itrace.listeners.EditorPartListener;
import edu.ysu.itrace.preferences.PluginPreferences;
import edu.ysu.itrace.visualization.GazeMap;
import edu.ysu.itrace.visualization.data.DataExtractor;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "edu.ysu.itrace"; //$NON-NLS-1$
    public File visFile;
    public DataExtractor extractor = new DataExtractor();
    // The shared instance
    private static Activator plugin;
    
    private static HashMap<IEditorPart,GazeMap>	gazeMaps = new HashMap<IEditorPart,GazeMap>();
    
    /**
     * The constructor
     */
    public Activator() {
    	PlatformUI.getWorkbench().getActiveWorkbenchWindow().getPartService().addPartListener(new EditorPartListener());
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
    
    public void updateEditor(IEditorPart editorPart){
    	if(editorPart == null){
    		editorPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
    		if(editorPart == null) return;
    	}
    	if(!gazeMaps.containsKey(editorPart)) gazeMaps.put(editorPart, new GazeMap(editorPart));
    	gazeMaps.get(editorPart).updateFile();
    }
    
    public void updateGazeMapCursorIndex(int index){
    	for(GazeMap map: gazeMaps.values()){
    		map.cursorIndex = index;
    		map.redraw();
    	}
    }
}
