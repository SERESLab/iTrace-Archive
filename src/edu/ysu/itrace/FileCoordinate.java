package edu.ysu.itrace;

public class FileCoordinate {
	public int line;
	public int column;
	public int x;
	public int y;
	public FileCoordinate(int line, int column, int x, int y){
		this.line = line;
		this.column = column;
		this.x = x;
		this.y = y;
	}
	@Override
	public String toString(){
		return "Line " + line + ", Column " + column + ", X " + x + ", Y " + y;
	}
}
