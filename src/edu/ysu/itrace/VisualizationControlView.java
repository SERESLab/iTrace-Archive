package edu.ysu.itrace;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class VisualizationControlView extends ViewPart {

	@Override
	public void createPartControl(Composite parent) {
		final Composite buttonComposite = new Composite(parent, SWT.NONE);
		buttonComposite.setLayout(new GridLayout(2,false));
		Button helloButton = new Button(buttonComposite,SWT.PUSH);
		helloButton.setText("Hello");
	}

	@Override
	public void setFocus() {
		//Do nothing.
	}

}
