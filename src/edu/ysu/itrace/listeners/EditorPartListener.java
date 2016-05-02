package edu.ysu.itrace.listeners;

import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;

import edu.ysu.itrace.Activator;

public class EditorPartListener implements IPartListener2 {

	@Override
	public void partActivated(IWorkbenchPartReference arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef) {
		if(partRef instanceof IEditorReference) 
			Activator.getDefault().updateEditor(((IEditorReference) partRef).getEditor(false));
	}

	@Override
	public void partClosed(IWorkbenchPartReference arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void partDeactivated(IWorkbenchPartReference arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void partHidden(IWorkbenchPartReference arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void partInputChanged(IWorkbenchPartReference arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void partOpened(IWorkbenchPartReference arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void partVisible(IWorkbenchPartReference arg0) {
		// TODO Auto-generated method stub

	}
	
	public EditorPartListener(){
	}

}
