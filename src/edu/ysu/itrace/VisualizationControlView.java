package edu.ysu.itrace;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class VisualizationControlView extends ViewPart {
	Button chooseFileButton;
	Button displayGazeMapButton;
	Button displayHeatMapButton;
	Label filenameLabel;
	
	Shell chooseFileShell = new Shell();
	Shell rootShell;
	
	class ChooseFile implements SelectionListener{		
		@Override
		public void widgetDefaultSelected(SelectionEvent arg0) {
			//Do nothing
		}

		@Override
		public void widgetSelected(SelectionEvent arg0) {
			FileDialog fileDialog = new FileDialog(chooseFileShell, SWT.OPEN);
			fileDialog.setText("Open");
			fileDialog.setFilterPath("C:/Users/Ben/runtime-iTrace");
			String chosenFileName = fileDialog.open();
			if(chosenFileName == null) return;
			filenameLabel.setText(chosenFileName);
			System.out.println(chosenFileName);
			try {
				FileCoordinate[] lines = XmlDataParser.parseFile(chosenFileName);
				ITrace.getDefault().lines = lines;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			for(IEditorReference er: PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences()){
				IEditorPart ep = er.getEditor(false);
				if(ep == null) continue;
				StyledText st = (StyledText)ep.getAdapter(Control.class);
				if(st == null) continue;
				st.redraw();	
			}
				
		}
		
	}
	
	class SetDisplays implements SelectionListener{

		@Override
		public void widgetDefaultSelected(SelectionEvent arg0) {
			//Do nothing.
			
		}

		@Override
		public void widgetSelected(SelectionEvent arg0) {
			ITrace.getDefault().displayGazeMap = displayGazeMapButton.getSelection();
			ITrace.getDefault().displayHeatMap = displayHeatMapButton.getSelection();
			
			for(IEditorReference er: PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences()){
				er.getEditor(false).getAdapter(Control.class).redraw();;	
			}
		}
		
	}
	SetDisplays setDisplays = new SetDisplays();

	@Override
	public void createPartControl(Composite parent) {
		
		chooseFileShell.setSize(400,400);
		
		final Composite topComposite = new Composite(parent, SWT.NONE);
		topComposite.setLayout(new GridLayout(2,false));
		
		filenameLabel = new Label(topComposite, 0);
		filenameLabel.setText("No File selected.");
		
		final Composite buttonComposite = new Composite(topComposite, SWT.NONE);
		buttonComposite.setLayout(new GridLayout(2,false));
		
		chooseFileButton = new Button(buttonComposite,SWT.PUSH);
		chooseFileButton.setText("Choose File");
		chooseFileButton.addSelectionListener(new ChooseFile());
		
		final Composite optionComposite = new Composite(topComposite, SWT.NONE);
		optionComposite.setLayout(new GridLayout(2,false));
		
		displayGazeMapButton = new Button(optionComposite,SWT.CHECK);
		displayGazeMapButton.setText("Display Gaze Map");
		displayGazeMapButton.addSelectionListener(setDisplays);
		
		displayHeatMapButton = new Button(optionComposite, SWT.CHECK);
		displayHeatMapButton.setText("Display Heat Map");
		displayHeatMapButton.addSelectionListener(setDisplays);
		
		final Composite legendComposite = new Composite(topComposite, SWT.NONE);
		legendComposite.setLayout(new GridLayout(5,false));
		
		Label ten = new Label(legendComposite,0);
		ten.setText("< 10%");
		ten.setBackground(new Color(null,0,153,204));
		
		Label twenty = new Label(legendComposite,0);
		twenty.setText("< 20%");
		twenty.setBackground(new Color(null,0,204,255));
		
		Label thirty = new Label(legendComposite,0);
		thirty.setText("< 30%");
		thirty.setBackground(new Color(null,0,255,255));
		
		Label forty = new Label(legendComposite,0);
		forty.setText("< 40%");
		forty.setBackground(new Color(null,0,255,153));
		
		Label fifty = new Label(legendComposite,0);
		fifty.setText("< 50%");
		fifty.setBackground(new Color(null,204,255,51));
		
		Label sixty = new Label(legendComposite,0);
		sixty.setText("< 60%");
		sixty.setBackground(new Color(null,255,255,102));
		
		Label seventy = new Label(legendComposite,0);
		seventy.setText("< 70%");
		seventy.setBackground(new Color(null,255,204,153));
		
		Label eighty = new Label(legendComposite,0);
		eighty.setText("< 80%");
		eighty.setBackground(new Color(null,255,153,102));
		
		Label ninety = new Label(legendComposite,0);
		ninety.setText("< 90%");
		ninety.setBackground(new Color(null,255,102,0));
		
		Label hundred = new Label(legendComposite,0);
		hundred.setText("< 100%");
		hundred.setBackground(new Color(null,255,0,0));
	}

	@Override
	public void setFocus() {
		//Do nothing.
	}

}
