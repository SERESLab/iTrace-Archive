package edu.ysu.itrace.filters;

public class SourceCodeEntity {
	private String name;
	private String type;
	private String how;
	private int length;
	private int startLine;
	private int endLine;
	private int startCol;
	private int endCol;
	
	public SourceCodeEntity(String name, String type,
			String how, int length, int startLine, int endLine,
			int startCol, int endCol) {
		
		this.name = name;
		this.type = type;
		this.how = how;
		this.length = length;
		this.startLine = startLine;
		this.endLine = endLine;
		this.startCol = startCol;
		this.endCol = endCol;
	}
	
	//Getters
	public String getName() {
		return name;
	}
	
	public String getType() {
		return type;
	}
	
	public String getHow() {
		return how;
	}
	
	public int getTotalLength() {
		return length;
	}
	
	public int getStartLine() {
		return startLine;
	}
	
	public int getEndLine() {
		return endLine;
	}
	
	public int getStartCol() {
		return startCol;
	}
	
	public int getEndCol() {
		return endCol;
	}
}
