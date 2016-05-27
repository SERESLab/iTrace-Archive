package edu.ysu.itrace.visualization;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import edu.ysu.itrace.Activator;

import java.io.File;



public class VisFrame extends JFrame {
	File dataFile = Activator.getDefault().visFile;
	
	public VisFrame(){
		super();
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
		HierarchyVis hVis = new HierarchyVis();
		JScrollPane visPane = new JScrollPane(hVis);
		add(visPane);
		setVisible(true);
	}
}
