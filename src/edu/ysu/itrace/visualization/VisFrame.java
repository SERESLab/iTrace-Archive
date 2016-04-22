package edu.ysu.itrace.visualization;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JFileChooser;

import java.io.File;



public class VisFrame extends JFrame {
	JFileChooser fileChooser;
	int fcValue;
	File dataFile;
	
	public VisFrame(){
		super();
		fileChooser = new JFileChooser();
		fcValue = fileChooser.showOpenDialog(this);
		if (fcValue == JFileChooser.APPROVE_OPTION){
			dataFile = fileChooser.getSelectedFile();
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
			HierarchyVis hVis = new HierarchyVis(dataFile);
			JScrollPane visPane = new JScrollPane(hVis);
			add(visPane);
			setVisible(true);
		}else{
			dispose();
		}
		
	}
}
