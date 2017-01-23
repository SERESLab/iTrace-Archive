package edu.ysu.itrace.preferences;

import java.util.Arrays;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import edu.ysu.itrace.ITrace;
import edu.ysu.itrace.EyeTrackerFactory;
import edu.ysu.itrace.EyeTrackerFactory.TrackerType;

public class PluginPreferences extends PreferencePage
                               implements IWorkbenchPreferencePage {
    public static final String
        EYE_TRACKER_TYPE = "eyeTrackerType";

    private List trackerList;

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    protected Control createContents(Composite parent) {
        Label trackerListLabel = new Label(parent, SWT.NONE);
        trackerListLabel.setText("Eye Tracker Interface");

        //Get currently selected eye tracker type as index into list.
        TrackerType[] trackerKeys = EyeTrackerFactory.getAvailableEyeTrackers()
                                    .keySet().toArray(new TrackerType[0]);
        int trackerSelectionIndex = Arrays.asList(trackerKeys).indexOf(
                TrackerType.valueOf(getPreferenceStore()
                .getString(EYE_TRACKER_TYPE)));

        //Create tracker list.
        trackerList = new List(parent, SWT.BORDER);
        String[] items = EyeTrackerFactory.getAvailableEyeTrackers().values()
                                          .toArray(new String[0]);
        trackerList.setItems(items);
        trackerList.setSelection(trackerSelectionIndex);

        return parent;
    }

    @Override
    protected IPreferenceStore doGetPreferenceStore() {
        return ITrace.getDefault().getPreferenceStore();
    }

    @Override
    public boolean performOk() {
        int selectionIndex = trackerList.getSelectionIndex();
        TrackerType[] trackerTypes = EyeTrackerFactory
                .getAvailableEyeTrackers().keySet()
                .toArray(new EyeTrackerFactory.TrackerType[0]);
        TrackerType trackerType = trackerTypes[selectionIndex];

        EyeTrackerFactory.setTrackerType(trackerType);
        getPreferenceStore().setValue(EYE_TRACKER_TYPE, trackerType.name());
        return true;
    }
}
