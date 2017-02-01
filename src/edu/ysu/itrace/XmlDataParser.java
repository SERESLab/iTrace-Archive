package edu.ysu.itrace;

import java.io.File;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlDataParser {
	static int[] parseFile(String filename) throws Exception{
		File dataFile = new File(filename);
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document dataDoc = builder.parse(dataFile);
		dataDoc.getDocumentElement().normalize();
		NodeList nodes = dataDoc.getElementsByTagName("response");
		int[] lines = new int[nodes.getLength()];
		System.out.println(nodes.getLength());
		for(int i=0; i<nodes.getLength(); i++){
			Node node = nodes.item(i);
			Element element = (Element) node;
			String line = element.getAttribute("line");
			//System.out.println(line);
			lines[i] = Integer.parseInt(line);
		}
		return lines;
	}
}
