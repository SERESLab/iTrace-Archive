package edu.ysu.itrace.filters.fixation;

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

import edu.ysu.itrace.filters.SourceCodeEntity;
import edu.ysu.itrace.filters.NewRawGaze;
import edu.ysu.itrace.filters.RawGaze;

public class XMLBasicFixationFilter extends BasicFixationFilter {
	
	//log file header variables
	private int width;
	private int height;
	
	private String fileDir;
	private String devUsername;
	private String sessionID;
	
	private final String filterName = "XML Fixation Filter";
	
	private ArrayList<RawGaze> rawGazes = new ArrayList<RawGaze>();
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
		rawGazes.clear(); //make sure rawGazes is empty
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
					if (file.exists()) {
						try {
							XMLInputFactory factory = XMLInputFactory.newInstance();
							XMLStreamReader reader = factory.createXMLStreamReader(
									new FileReader(file.getAbsolutePath()));
				
							while (reader.hasNext()) {
								int event = reader.next();
								if (event == XMLStreamConstants.START_ELEMENT) {
									if (reader.getLocalName().equals("screen-size")) {
										width = Integer.parseInt(reader.getAttributeValue(0));
										height = Integer.parseInt(reader.getAttributeValue(1));
									} else if (reader.getLocalName().equals("response")) {
										rawGazes.add(getRawGaze(reader));
									} else {
										//do nothing
									}
								}
						
							}
							setRawGazes(rawGazes);
						} catch (XMLStreamException e) {
							throw new IOException("Could not read in data." +
									getFilterName() + ".");
						}
					}
				}
			}
		}
	}
	
	public NewRawGaze getRawGaze(XMLStreamReader reader) throws XMLStreamException {
		String file = reader.getAttributeValue(0);
		String type = reader.getAttributeValue(1);
		double x = Double.parseDouble(reader.getAttributeValue(2));
		double y = Double.parseDouble(reader.getAttributeValue(3));
		double leftValidity = Double.parseDouble(reader.getAttributeValue(4));
		double rightValidity = Double.parseDouble(reader.getAttributeValue(5));
		double leftPupilDiam = Double.parseDouble(reader.getAttributeValue(6));
		double rightPupilDiam = Double.parseDouble(reader.getAttributeValue(7));
		String timeStamp = reader.getAttributeValue(8);
		long sessionTime = Long.parseLong(reader.getAttributeValue(9));
		long trackerTime = Long.parseLong(reader.getAttributeValue(10));
		long systemTime = Long.parseLong(reader.getAttributeValue(11));
		long nanoTime = Long.parseLong(reader.getAttributeValue(12));
		String path = reader.getAttributeValue(13);
		int lineHeight = Integer.parseInt(reader.getAttributeValue(14));
		int fontHeight = Integer.parseInt(reader.getAttributeValue(15));
		int line = Integer.parseInt(reader.getAttributeValue(16));
		int col = Integer.parseInt(reader.getAttributeValue(17));
		int lineBaseX = Integer.parseInt(reader.getAttributeValue(18));
		int lineBaseY = Integer.parseInt(reader.getAttributeValue(19));
		ArrayList<SourceCodeEntity> sces = new ArrayList<SourceCodeEntity>();
		
		reader.next();
		reader.next();
		while(!reader.isEndElement()) {
			String name = reader.getAttributeValue(0);
			String Type = reader.getAttributeValue(1);
			String how = reader.getAttributeValue(2);
			int length = Integer.parseInt(reader.getAttributeValue(3));
			int startLine = Integer.parseInt(reader.getAttributeValue(4));
			int endLine = Integer.parseInt(reader.getAttributeValue(5));
			int startCol = Integer.parseInt(reader.getAttributeValue(6));
			int endCol = Integer.parseInt(reader.getAttributeValue(7));
			
			sces.add(new SourceCodeEntity(name, Type, how, length,
					startLine, endLine, startCol, endCol));
			reader.next();
			reader.next();
		}
		
		return new NewRawGaze(file, type, x, y, leftValidity, rightValidity,
				leftPupilDiam, rightPupilDiam, timeStamp, sessionTime,
				trackerTime, systemTime, nanoTime, path, lineHeight,
				fontHeight, lineBaseX, line, col, lineBaseY, sces);
	}
	
	@Override
	public void export() throws IOException {
		if (getProcessedGazes() != null && getRawGazeFixations() != null) {
			//export raw gazes with associated fixations
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
								factory.createXMLStreamWriter(new FileOutputStream(outFile), "UTF-8");
						
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
						for (final NewRawGazeFixation rawGaze : getRawGazeFixations()) {
							writer.writeStartElement("response");
			                writer.writeAttribute("name", rawGaze.getFile());
			                writer.writeAttribute("type", rawGaze.getType());
			                writer.writeAttribute("x", String.valueOf(rawGaze.getX()));
			                writer.writeAttribute("y", String.valueOf(rawGaze.getY()));
			                writer.writeAttribute("left-validation",
			                        String.valueOf(rawGaze.getLeftValid()));
			                writer.writeAttribute("right-validation",
			                        String.valueOf(rawGaze.getRightValid()));
			                writer.writeAttribute("left_pupil_diameter",
			                        String.valueOf(rawGaze.getLeftPupilDiam()));
			                writer.writeAttribute("right_pupil_diameter",
			                        String.valueOf(rawGaze.getRightPupilDiam()));
			                writer.writeAttribute("timestamp",	rawGaze.getTimeStamp());
			                writer.writeAttribute("session_time",
			                		String.valueOf(rawGaze.getSessionTime()));
			                writer.writeAttribute("tracker-time",
			                        String.valueOf(rawGaze.getTrackerTime()));
			                writer.writeAttribute("system-time",
			                        String.valueOf(rawGaze.getSystemTime()));
			                writer.writeAttribute("nano-time",
			                        String.valueOf(rawGaze.getNanoTime()));
			                writer.writeAttribute("path", rawGaze.getPath());
			                writer.writeAttribute("line_height",
			                		String.valueOf(rawGaze.getLineHeight()));
			                writer.writeAttribute("font_height",
			                		String.valueOf(rawGaze.getFontHeight()));
			                writer.writeAttribute("line",
			                		String.valueOf(rawGaze.getLine()));
			                writer.writeAttribute("col",
			                		String.valueOf(rawGaze.getCol()));
			                writer.writeAttribute("line_base_x",
			                		String.valueOf(rawGaze.getLineBaseX()));
			                writer.writeAttribute("line_base_y",
			                		String.valueOf(rawGaze.getLineBaseY()));
			                writer.writeAttribute("fix_index",
			                		String.valueOf(rawGaze.getFixIndex()));
			                writer.writeAttribute("fix_x",
			                		String.valueOf(rawGaze.getFixX()));
			                writer.writeAttribute("fix_y",
			                		String.valueOf(rawGaze.getFixY()));
			                writer.writeAttribute("duration",
			                		String.valueOf(rawGaze.getDuration()));
			                writer.writeStartElement("sces");
			                
			                for (final SourceCodeEntity sce : rawGaze.getSces()) {
			                	writer.writeStartElement("sce");
			                	writer.writeAttribute("name", sce.getName());
			                	writer.writeAttribute("type", sce.getType());
			                	writer.writeAttribute("how", sce.getHow());
			                	writer.writeAttribute("total_length",
			                			String.valueOf(sce.getTotalLength()));
			                	writer.writeAttribute("start_line",
			                			String.valueOf(sce.getStartLine()));
			                	writer.writeAttribute("end_line",
			                			String.valueOf(sce.getEndLine()));
			                	writer.writeAttribute("start_col",
			                			String.valueOf(sce.getStartCol()));
			                	writer.writeAttribute("end_col",
			                			String.valueOf(sce.getEndCol()));
			                	writer.writeEndElement();
			                }
			                writer.writeEndElement();
			                writer.writeEndElement();
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
				
			//export fixations
			outFile = new File(fileDir + "/processed-fixations-"
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
								factory.createXMLStreamWriter(new FileOutputStream(outFile), "UTF-8");
						
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
					    writer.writeStartElement("fixations");
					    writer.writeCharacters(EOL);
						
						//export processed fixations
						for (final Fixation fixation : getProcessedGazes()) {
							writer.writeStartElement("response");
			                writer.writeAttribute("name", fixation.getRawGaze().getFile());
			                writer.writeAttribute("type", fixation.getRawGaze().getType());
			                writer.writeAttribute("x", String.valueOf(fixation.getRawGaze().getX()));
			                writer.writeAttribute("y", String.valueOf(fixation.getRawGaze().getY()));
			                writer.writeAttribute("left-validation",
			                        String.valueOf(fixation.getRawGaze().getLeftValid()));
			                writer.writeAttribute("right-validation",
			                        String.valueOf(fixation.getRawGaze().getRightValid()));
			                writer.writeAttribute("left_pupil_diameter",
			                        String.valueOf(fixation.getRawGaze()
			                                       .getLeftPupilDiam()));
			                writer.writeAttribute("right_pupil_diameter",
			                        String.valueOf(fixation.getRawGaze()
			                                       .getRightPupilDiam()));
			                writer.writeAttribute("timestamp",
			                		((NewRawGaze)fixation.getRawGaze()).getTimeStamp());
			                writer.writeAttribute("session_time",
			                		String.valueOf(((NewRawGaze)
			                				fixation.getRawGaze()).getSessionTime()));
			                writer.writeAttribute("tracker-time",
			                        String.valueOf(fixation.getRawGaze().getTrackerTime()));
			                writer.writeAttribute("system-time",
			                        String.valueOf(fixation.getRawGaze().getSystemTime()));
			                writer.writeAttribute("nano-time",
			                        String.valueOf(fixation.getRawGaze().getNanoTime()));
			                writer.writeAttribute("path",
			                		((NewRawGaze)fixation.getRawGaze()).getPath());
			                writer.writeAttribute("line_height",
			                		String.valueOf(((NewRawGaze)
			                				fixation.getRawGaze()).getLineHeight()));
			                writer.writeAttribute("font_height",
			                		String.valueOf(((NewRawGaze)
			                				fixation.getRawGaze()).getFontHeight()));
			                writer.writeAttribute("line",
			                		String.valueOf(fixation.getRawGaze().getLine()));
			                writer.writeAttribute("col",
			                		String.valueOf(fixation.getRawGaze().getCol()));
			                writer.writeAttribute("line_base_x",
			                		String.valueOf(fixation.getRawGaze().getLineBaseX()));
			                writer.writeAttribute("line_base_y",
			                		String.valueOf(fixation.getRawGaze().getLineBaseY()));
			                writer.writeAttribute("fix_index",
			                		String.valueOf(fixation.getFixIndex()));
			                writer.writeAttribute("duration",
			                		String.valueOf(fixation.getDuration()));
			                writer.writeStartElement("sces");
			                
			                for (final SourceCodeEntity sce : ((NewRawGaze)fixation.getRawGaze())
			                		.getSces()) {
			                	writer.writeStartElement("sce");
			                	writer.writeAttribute("name", sce.getName());
			                	writer.writeAttribute("type", sce.getType());
			                	writer.writeAttribute("how", sce.getHow());
			                	writer.writeAttribute("total_length",
			                			String.valueOf(sce.getTotalLength()));
			                	writer.writeAttribute("start_line",
			                			String.valueOf(sce.getStartLine()));
			                	writer.writeAttribute("end_line",
			                			String.valueOf(sce.getEndLine()));
			                	writer.writeAttribute("start_col",
			                			String.valueOf(sce.getStartCol()));
			                	writer.writeAttribute("end_col",
			                			String.valueOf(sce.getEndCol()));
			                	writer.writeEndElement();
			                }
			                writer.writeEndElement();
			                writer.writeEndElement();
			                writer.writeCharacters(EOL);
						}
					} catch ( XMLStreamException e) {
						throw new IOException("Failed to write processed fixations to file.");
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
								System.out.println("Processed fixations saved.");
							}
						} catch (XMLStreamException e) {
							throw new IOException("Failed to write processed fixations to file.");
						}
					}
				}
		}
	}
}
