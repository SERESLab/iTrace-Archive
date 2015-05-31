package edu.ysu.itrace.filters;

import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

public class JSONBasicFixationFilter extends BasicFixationFilter {
	
	@Override
	public void init() throws IOException {
		File[] fileList = getFileList();
		if (fileList != null) {
			for (File file : fileList) {
				if(file.getName().lastIndexOf(".") > 0) {
					int i = file.getName().lastIndexOf(".");
					if (file.getName().substring(i+1) == "json") {
						
						if (file.exists()) {
							
						}
					}
				}
			}
		} else {
			JOptionPane.showMessageDialog(null,
					"You have not selected "
					+ "any files to process!", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}
	
	@Override
	public void export() {
		
	}
	
	@Override
	public void dispose() throws IOException {
		
	}
}
