package edu.ysu.itrace.visualization;

import java.awt.Rectangle;

import java.awt.Color;

public class AOISection extends Rectangle {
	public boolean hasChildren;
	public String name;
	
	public AOISection(int x,int y, int width, int height, String n){
		super(x,y,width,height);
		name = n;
	}
}
