package edu.ysu.itrace.filters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

public class XMLBasicFixationFilter extends BasicFixationFilter {
	
	//log file header variables
	private int width;
	private int height;
	
	private String fileDir;
	private String devUsername;
	private String sessionID;
	
	private final String filterName = "XML Fixation Filter";
	
	private ArrayList<RawGaze> rawGazes;
	private static final String EOL = System.getProperty("line.separator");
	
	@Override
	public File[] filterUI() {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"XML Files", "xml");
		chooser.setFileFilter(filter);
		chooser.setMultiSelectionEnabled(true);
		int returnVal = chooser.showOpenDialog(null);
		if(returnVal == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFiles();
		} else {
			return null;
		}
	}
	
	@Override
	public String getFilterName() {
		return filterName;
	}
	
	@Override
	public void read(File file) throws IOException {
		if (file != null) {
			//Set up needed export data
			fileDir = new String(file.getParent());
			String[] parts = file.getName().split("-");
			devUsername = parts[2];
			sessionID = parts[3] + "-" + parts[4] + "-" + parts[5].split(Pattern.quote("."))[0];
			
			//Read from file
			if(file.getName().lastIndexOf(".") > 0) {
				int i = file.getName().lastIndexOf(".");
				if (file.getName().substring(i+1).equals("xml")) {
					if (file.exists()) { System.out.println("here");
						try {
							XMLInputFactory factory = XMLInputFactory.newInstance();
							XMLStreamReader reader = factory.createXMLStreamReader(
									new FileReader(file.getAbsolutePath()));
				
							while (reader.hasNext()) {
								int event = reader.next();
								switch(event) {
									case XMLStreamConstants.START_ELEMENT:
										if (reader.getLocalName().equals("screen-size")) {
											width = Integer.parseInt(reader.getAttributeValue(0));
											height = Integer.parseInt(reader.getAttributeValue(1));
											break;
										} else if (reader.getLocalName().equals("response")) {
											rawGazes.add(getRawGaze(reader));
											break;
										} else {
											break;
										}
									case XMLStreamConstants.START_DOCUMENT:
										rawGazes = new ArrayList<RawGaze>();
										break;
								}
						
							}
						} catch (XMLStreamException e) {
							throw new IOException("Could not read in data." +
									getFilterName() + ".");
						}
					}
				}
			}
		}
	}
	
	public RawGaze getRawGaze(XMLStreamReader reader) {
		String file = reader.getAttributeValue(0);
		String type = reader.getAttributeValue(1);
		double x = Double.parseDouble(reader.getAttributeValue(2));
		double y = Double.parseDouble(reader.getAttributeValue(3));
		double leftValidity = Double.parseDouble(reader.getAttributeValue(4));
		double rightValidity = Double.parseDouble(reader.getAttributeValue(5));
		double leftPupilDiam = Double.parseDouble(reader.getAttributeValue(6));
		double rightPupilDiam = Double.parseDouble(reader.getAttributeValue(8));
		long trackerTime = Integer.parseInt(reader.getAttributeValue(9));
		long systemTime = Integer.parseInt(reader.getAttributeValue(10));
		long nanoTime = Integer.parseInt(reader.getAttributeValue(11));
		int lineBaseX = Integer.parseInt(reader.getAttributeValue(12));
		int line = Integer.parseInt(reader.getAttributeValue(13));
		int col = Integer.parseInt(reader.getAttributeValue(14));
		String hows = null;
		String types = null;
		String fullyQualifiedNames = null;
		int lineBaseY = -1;
		
		if (reader.getAttributeCount() == 19) {
			hows = reader.getAttributeValue(15);
			types = reader.getAttributeValue(16);
			fullyQualifiedNames = reader.getAttributeValue(17);
			lineBaseY = Integer.parseInt(reader.getAttributeValue(18));
		} else {
			lineBaseY = Integer.parseInt(reader.getAttributeValue(15));
		}
		
		return new RawGaze(file, type, x, y, leftValidity, rightValidity,
				leftPupilDiam, rightPupilDiam, trackerTime, systemTime,
				nanoTime, lineBaseX, line, col, hows, types, fullyQualifiedNames,
				lineBaseY);
	}
	
	@Override
	public void export() throws IOException {
		if (getProcessedGazes() != null) {
			File outFile = new File(fileDir + "/processed-gazes-"
					+ devUsername + "-" + sessionID + ".xml");
				if (outFile.exists()) {
					System.out.println("You cannot overwrite this file. If you "
							+ "wish to continue, delete the file " + "manually.");
					return;
				} else {
					System.out.println("Putting files at "
							+ outFile.getAbsolutePath());
					outFile.createNewFile();
					
					//export to new file
					XMLStreamWriter writer = null;
					try {
						XMLOutputFactory factory = XMLOutputFactory.newInstance();
						writer =
								factory.createXMLStreamWriter(new FileOutputStream(outFile), "UTF-8");;
						
						//export header
						writer.writeStartDocument("utf-8", "1.0");
					    writer.writeCharacters(EOL);
					    writer.writeStartElement("itrace-records");
					    writer.writeCharacters(EOL);
					    writer.writeStartElement("environment");
					    writer.writeCharacters(EOL);
					    writer.writeEmptyElement("screen-size");
					    writer.writeAttribute("width",
					            String.valueOf(width));
					    writer.writeAttribute("height",
					            String.valueOf(height));
					    writer.writeCharacters(EOL);
					    writer.writeEndElement();
					    writer.writeCharacters(EOL);
					    writer.writeStartElement("gazes");
					    writer.writeCharacters(EOL);
						
						//export processed gazes
						for (Fixation fixation : getProcessedGazes()) {
							writer.writeEmptyElement("response");
			                writer.writeAttribute("file", fixation.getRawGaze().getFile());
			                writer.writeAttribute("type", fixation.getRawGaze().getType());
			                writer.writeAttribute("x", String.valueOf(fixation.getRawGaze().getX()));
			                writer.writeAttribute("y", String.valueOf(fixation.getRawGaze().getY()));
			                writer.writeAttribute("left-validation",
			                        String.valueOf(fixation.getRawGaze().getLeftValid()));
			                writer.writeAttribute("right-validation",
			                        String.valueOf(fixation.getRawGaze().getRightValid()));
			                writer.writeAttribute("left-pupil-diameter",
			                        String.valueOf(fixation.getRawGaze()
			                                       .getLeftPupilDiam()));
			                writer.writeAttribute("right-pupil-diameter",
			                        String.valueOf(fixation.getRawGaze()
			                                       .getRightPupilDiam()));
			                writer.writeAttribute(
			                        "tracker-time",
			                        String.valueOf(fixation.getRawGaze().getTrackerTime()));
			                writer.writeAttribute(
			                        "system-time",
			                        String.valueOf(fixation.getRawGaze().getSystemTime()));
			                writer.writeAttribute(
			                        "nano-time",
			                        String.valueOf(fixation.getRawGaze().getNanoTime()));
			                writer.writeAttribute("duration",
			                		String.valueOf(fixation.getDuration()));
			                writer.writeAttribute("line_base_x",
			                		String.valueOf(fixation.getRawGaze().getLineBaseX()));
			                writer.writeAttribute("line_base_y",
			                		String.valueOf(fixation.getRawGaze().getLineBaseY()));
			                writer.writeAttribute("line",
			                		String.valueOf(fixation.getRawGaze().getLine()));
			                writer.writeAttribute("col",
			                		String.valueOf(fixation.getRawGaze().getCol()));
			                writer.writeAttribute("hows", fixation.getRawGaze().getHows());
			                writer.writeAttribute("types", fixation.getRawGaze().getTypes());
			                writer.writeAttribute("fullyQualifiedNames",
			                		fixation.getRawGaze().getFullyQualifiedNames());
			                writer.writeCharacters(EOL);
						}
					} catch ( XMLStreamException e) {
						throw new IOException("Failed to write processed gazes to file.");
					}
					finally {
						try {
							if (writer != null) {
								writer.writeEndElement();
					            writer.writeCharacters(EOL);
					            writer.writeEndElement();
					            writer.writeCharacters(EOL);
					            writer.writeEndDocument();
					            writer.writeCharacters(EOL);
					            writer.flush();
					            writer.close();
								System.out.println("Processed gazes saved.");
							}
						} catch (XMLStreamException e) {
							throw new IOException("Failed to write processed gazes to file.");
						}
					}
				}
		}
	}
}