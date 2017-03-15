package edu.ysu.itrace;

public class FileCoordinate {
	public String filename;
	public int line;
	public int column;
	public FileCoordinate(String filename,int line, int column){
		this.filename = filename;
		this.line = line;
		this.column = column;
	}
	@Override
	public String toString(){
		return "Line " + line + ", Column " + column;
	}
}
