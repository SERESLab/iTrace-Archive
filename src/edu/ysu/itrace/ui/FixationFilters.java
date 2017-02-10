package edu.ysu.itrace.ui;

import javax.swing.*;

public abstract class FixationFilters{
	protected JPanel filter;

	public JPanel getFilter(){
		return filter;
	}
	public abstract String filterName();
	public abstract void applyChanges();
}
