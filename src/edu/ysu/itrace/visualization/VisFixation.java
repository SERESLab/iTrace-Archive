package edu.ysu.itrace.visualization;

public class VisFixation {
	public String 	file;
	public int 		line;
	public int 		column;
	public int 		duration;
	
	public VisFixation(String file, int line, int column, int duration){
		this.file = file;
		this.line = line;
		this.column = column;
		this.duration = duration;
	}
	public DocLine getDocLine(){
		return new DocLine(file,line);
	}
}
