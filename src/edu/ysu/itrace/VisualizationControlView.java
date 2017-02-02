package edu.ysu.itrace;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.ViewPart;

public class VisualizationControlView extends ViewPart {
	class ChooseFile implements SelectionListener{

		@Override
		public void widgetDefaultSelected(SelectionEvent arg0) {
			//Do nothing/
		}

		@Override
		public void widgetSelected(SelectionEvent arg0) {
			FileDialog fileDialog = new FileDialog(chooseFileShell, SWT.OPEN);
			fileDialog.setText("Open");
			fileDialog.setFilterPath("C:/Users/Ben/runtime-iTrace");
			String chosenFileName = fileDialog.open();
			System.out.println(chosenFileName);
			try {
				FileCoordinate[] lines = XmlDataParser.parseFile(chosenFileName);
				ITrace.getDefault().lines = lines;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	Shell chooseFileShell = new Shell();
	
	@Override
	public void createPartControl(Composite parent) {
		chooseFileShell.setSize(400,400);
		final Composite buttonComposite = new Composite(parent, SWT.NONE);
		buttonComposite.setLayout(new GridLayout(2,false));
		Button chooseFileButton = new Button(buttonComposite,SWT.PUSH);
		chooseFileButton.setText("Choose File");
		chooseFileButton.addSelectionListener(new ChooseFile());
	}

	@Override
	public void setFocus() {
		//Do nothing.
	}

}
