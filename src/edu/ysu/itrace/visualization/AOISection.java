package edu.ysu.itrace.visualization;

import java.awt.Rectangle;

import java.awt.Color;

public class AOISection extends Rectangle {
	public boolean hasChildren;
	public Color fillColor;
	public String name;
	
	public AOISection(int x,int y, int width, int height, boolean hc, String n){
		super(x,y,width,height);
		hasChildren = hc;
		name = n;
		if (hasChildren){
			fillColor = Color.cyan;
		}else{
			fillColor = Color.lightGray;
		}
	}
}
