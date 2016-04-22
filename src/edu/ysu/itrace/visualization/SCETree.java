package edu.ysu.itrace.visualization;

import java.util.ArrayList;

public class SCETree {
	public String name, file;
	public int startLine, endLine, childCount;
	private SCETree parent;
	public boolean isRoot = false;
	
	private ArrayList<SCETree> children;
	
	public int ChildSearch(SCETree entity){
		for(int i=0;i<childCount;i++)
			if(entity.startLine == children.get(i).startLine && entity.endLine == children.get(i).endLine) return i;
		return -1;
	}
	public int findOrAdd(SCETree entity){
		int index = ChildSearch(entity);
		if(index == -1){
			for(index=0;index<childCount;index++)
				if(children.get(index).startLine > entity.startLine) break;
			children.add(index,entity);
			childCount++;
		}
		return index;
	}
	public boolean contains(DocLine docLine){
		return (docLine.line >= startLine && docLine.line <= endLine && file.equals(docLine.doc));
	}
	public boolean hasChildren(){
		return ( childCount > 0 );
	}
	public SCETree getChild(int index){
		return children.get(index);
	}
	public SCETree getParent(){
		return parent;
	}
	public String getName(){
		return name;
	}
	
	public SCETree(String givenName, String givenFile,SCETree givenParent, int start, int end){
		name = givenName;
		file = givenFile;
		parent = givenParent;
		startLine = start;
		endLine = end;
		childCount = 0;
		children = new ArrayList<SCETree>(2);
	}
	
	public SCETree(){
		name = "/";
		file = null;
		parent = null;
		isRoot = true;
		childCount = 0;
		children = new ArrayList<SCETree>(2);
	}
}
