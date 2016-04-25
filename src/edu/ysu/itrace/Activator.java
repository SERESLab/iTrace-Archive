package edu.ysu.itrace;

import java.io.File;
import java.util.HashMap;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import edu.ysu.itrace.preferences.PluginPreferences;
import edu.ysu.itrace.visualization.GazeMap;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "edu.ysu.itrace"; //$NON-NLS-1$
    public static File visFile;

    // The shared instance
    private static Activator plugin;
    
    private static HashMap<IEditorPart,GazeMap>	gazeMaps;
    
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

}
