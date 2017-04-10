package edu.ysu.itrace;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlDataParser {
	static FileCoordinate[] parseFile(String filename) throws Exception{
		File dataFile = new File(filename);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document dataDoc = builder.parse(dataFile);
		dataDoc.getDocumentElement().normalize();
		NodeList nodes = dataDoc.getElementsByTagName("response");
		FileCoordinate[] lines = new FileCoordinate[nodes.getLength()];
		System.out.println(nodes.getLength());
		for(int i=0; i<nodes.getLength(); i++){
			Node node = nodes.item(i);
			Element element = (Element) node;
			if(!element.getAttribute("type").equals("java")) continue;
			String line = element.getAttribute("line");
			String column = element.getAttribute("char_index");
			String name = element.getAttribute("name");
			String path = element.getAttribute("path");
			
			lines[i] = new FileCoordinate(name, Integer.parseInt(line),Integer.parseInt(column));
		}
		return lines;
	}
}
