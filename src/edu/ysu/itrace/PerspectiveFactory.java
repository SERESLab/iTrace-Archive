package edu.ysu.itrace;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * Manages the Eclipse perspective for this plugin.
 */
public class PerspectiveFactory implements IPerspectiveFactory {
    private static final float PROJECT_EXPOLORER_RATIO = 0.20f;
    private static final float BOTTOM_FOLDER_RATIO = 0.75f;

    private static final String BOTTOM_FOLDER_ID
            = "edu.ysu.itrace.BottomFolder";
    private static final String CONTROL_VIEW = "edu.ysu.itrace.controlview";


    @Override
    public void createInitialLayout(IPageLayout layout) {
        // set up package explorer
        layout.addView(IPageLayout.ID_PROJECT_EXPLORER, IPageLayout.LEFT,
                PROJECT_EXPOLORER_RATIO, layout.getEditorArea());

        // set up bottom folder
        IFolderLayout bottomFolder = layout.createFolder(BOTTOM_FOLDER_ID,
                IPageLayout.BOTTOM, BOTTOM_FOLDER_RATIO,
                layout.getEditorArea());
        bottomFolder.addView(IPageLayout.ID_PROBLEM_VIEW);
        bottomFolder.addView(IPageLayout.ID_PROP_SHEET);
        bottomFolder.addView(IPageLayout.ID_PROGRESS_VIEW);
        bottomFolder.addView(CONTROL_VIEW);
    }
}
