package edu.ysu.itrace.visualization.data;

import java.io.File;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.*;

import edu.ysu.itrace.visualization.DocLine;
import edu.ysu.itrace.visualization.SCETree;
import edu.ysu.itrace.visualization.VisFixation;

public class DataExtractor {
	private File 					file;
	private Document				doc;
	private ArrayList<Node> 		responses = new ArrayList<Node>();
	private ArrayList<VisFixation>	VisFixations = new ArrayList<VisFixation>();
	private SCETree					root;
	
	public DataExtractor(){
	}
	
	private void extract(){
		try{
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			doc = dBuilder.parse(file);
			doc.getDocumentElement().normalize();
			responses = NodeListToArrayList(responses, doc.getElementsByTagName("response"));
			SCETree currentTree = root;
			//Create Fixations and add File Trees
			for(Node response: responses){
				NamedNodeMap attributes = response.getAttributes();
				String name = getFileName(attributes.getNamedItem("path").getNodeValue());
				int line = Integer.parseInt(attributes.getNamedItem("line").getNodeValue());
				int column = Integer.parseInt(attributes.getNamedItem("col").getNodeValue());
				int duration = Integer.parseInt(attributes.getNamedItem("duration").getNodeValue());
				VisFixations.add(new VisFixation(name,line,column,duration));
				currentTree.findOrAdd(new SCETree(name,root));
			}
			//Add sce trees
			for(Node response: responses){
				NamedNodeMap attributes = response.getAttributes();
				String name = getFileName(attributes.getNamedItem("path").getNodeValue());
				Node scesNode = response.getFirstChild();
				if(scesNode.hasChildNodes()){
					int fileTreeIndex = root.ChildSearch(new SCETree(name,root));
					ArrayList<Node> sces = new ArrayList<Node>();
					sces = NodeListToArrayListReversed(sces,scesNode.getChildNodes());
					currentTree = root.getChild(fileTreeIndex);
					for(Node sce: sces){
						NamedNodeMap sceAttributes = sce.getAttributes();
						String sceName = sceAttributes.getNamedItem("name").getNodeValue();
						int startLine = Integer.parseInt(sceAttributes.getNamedItem("start_line").getNodeValue());
						int endLine = Integer.parseInt(sceAttributes.getNamedItem("end_line").getNodeValue());
						int childIndex = currentTree.findOrAdd(new SCETree(sceName,name,currentTree,startLine,endLine));
						currentTree = currentTree.getChild(childIndex);
					}
				}
			}
			currentTree = root;
			//Add line leaves
			for(int i=0;i<responses.size();){
				boolean found = false;
				int j;
				for(j=0;j<currentTree.childCount;j++){
					if(currentTree.getChild(j).contains(VisFixations.get(i).getDocLine())){
						found = true;
						break;
					}	
				}
				if(found) currentTree = currentTree.getChild(j);
				else{
					VisFixation vf = VisFixations.get(i);
					if(!(currentTree.startLine == vf.line && currentTree.endLine == vf.line))
						currentTree.findOrAdd(new SCETree("Line "+vf.line,vf.file,currentTree,vf.line,vf.line));
					currentTree = root;
					i++;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private ArrayList<Node> NodeListToArrayList(ArrayList<Node> arraylist,NodeList nodelist){
		for(int i=0;i<nodelist.getLength();i++) arraylist.add(nodelist.item(i));
		return arraylist;
	}
	private ArrayList<Node> NodeListToArrayListReversed(ArrayList<Node> arraylist,NodeList nodelist){
		for(int i=nodelist.getLength()-1;i>=0;i--) arraylist.add(nodelist.item(i));
		return arraylist;
	}
	
	private String getFileName(String path){
		String name = path;
		int place = 0;
		for(int i=0;i<name.length();i++)
			if((int)name.charAt(i) == 92) place = i;
		if(place != 0) name = name.substring(place+1);
		return name;
	}
	
	public void setFile(File file){
		this.file = file;
		root = new SCETree();
		extract();
	}
	
	public ArrayList<Node> getResponses(){
		return responses;
	}
	public ArrayList<VisFixation> getFixations(){
		return VisFixations;
	}
	public ArrayList<DocLine> getDocLines(){
		ArrayList<DocLine> dls = new ArrayList<DocLine>();
		for(VisFixation vf: VisFixations){
			dls.add(vf.getDocLine());
		}
		return dls;
	}
	public SCETree getSCETree(){
		return root;
	}
	
	
}
