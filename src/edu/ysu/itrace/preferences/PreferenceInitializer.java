package edu.ysu.itrace.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import edu.ysu.itrace.Activator;
import edu.ysu.itrace.EyeTrackerFactory;

/**
 * Initialises default preferences for plugin.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {
    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        store.setDefault(PluginPreferences.EYE_TRACKER_TYPE,
                EyeTrackerFactory.TrackerType.SYSTEM_MOUSE_TRACKER.name());
    }
}
