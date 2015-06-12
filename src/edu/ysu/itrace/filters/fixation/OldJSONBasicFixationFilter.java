package edu.ysu.itrace.filters.fixation;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import edu.ysu.itrace.filters.OldRawGaze;
import edu.ysu.itrace.filters.RawGaze;


public class OldJSONBasicFixationFilter extends BasicFixationFilter {
	
	//log file header variables
	private int width;
	private int height;
	
	private String fileDir;
	private String devUsername;
	private String sessionID;
	
	private final String filterName = "Old JSON Fixation Filter";
	
	@Override
	public File[] filterUI() {
		JFileChooser chooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter(
				"JSON Files", "json");
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
			sessionID = parts[3] + "-" + parts[4].split(Pattern.quote("."))[0];
				
			//Read from file
			if(file.getName().lastIndexOf(".") > 0) {
				int i = file.getName().lastIndexOf(".");
				if (file.getName().substring(i+1).equals("json")) {
						
					if (file.exists()) {
						try {
							JsonReader reader = new JsonReader(new FileReader(file.getAbsolutePath()));
							reader.beginObject();
				
							while (reader.hasNext()) {
								String name = reader.nextName();
					
								if (name.equals("environment")) {
									setLogInfo(reader);
								} else if (name.equals("gazes")) {
									setRawGazes(reader);
								} else {
									reader.skipValue();
								}
							}
							reader.endObject();
							reader.close();
						} catch (IOException e) {
							throw new IOException("Could not read in data." +
									getFilterName() + ".");
						}
					}
				}
			}
		}
	}
		
	public void setLogInfo(JsonReader reader) throws IOException {
		reader.beginObject();
		while (reader.hasNext()) {
			String name = reader.nextName();
			if (name.equals("screen_size")) {
				reader.beginObject();
				while(reader.hasNext()) {
					String name2 = reader.nextName();
					if (name2.equals("width")) {
						width = reader.nextInt();
					} else if (name2.equals("height")) {
						height = reader.nextInt();
					} else {
						reader.skipValue();
					}
				}
				reader.endObject();
			} else {
				reader.skipValue();
			}
		}
		reader.endObject();
	}
	
	public void setRawGazes(JsonReader reader) throws IOException {
		ArrayList<RawGaze> rawGazes = new ArrayList<RawGaze>();
		reader.beginArray();
		while (reader.hasNext()) {
			rawGazes.add(getRawGaze(reader));
		}
		reader.endArray();
		setRawGazes(rawGazes);
	}
	
	public OldRawGaze getRawGaze(JsonReader reader) throws IOException {
		String file = null;
		String type = null;
		double x = -1;
		double y = -1;
		double leftValidity = -1;
		double rightValidity = -1;
		double leftPupilDiam = -1;
		double rightPupilDiam = -1;
		long trackerTime = -1;
		long systemTime = -1;
		long nanoTime = -1;
		int lineBaseX = -1;
		int line = -1;
		int col = -1;
		String hows = new String();
		String types = new String();
		String fullyQualifiedNames = new String();
		int lineBaseY = -1;
		
		reader.beginObject();
		while (reader.hasNext()) {
			String name = reader.nextName();
			if (name.equals("file")) {
				file = reader.nextString();
			} else if (name.equals("type")) {
				type = reader.nextString();
			} else if (name.equals("x")) {
				x = reader.nextDouble();
			} else if (name.equals("y")) {
				y = reader.nextDouble();
			} else if (name.equals("left_validation")) {
				leftValidity = reader.nextDouble();
			} else if (name.equals("right_validation")) {
				rightValidity = reader.nextDouble();
			} else if (name.equals("left-pupil-diameter")) {
				leftPupilDiam = reader.nextDouble();
			} else if (name.equals("right-pupil-diameter")) {
				rightPupilDiam = reader.nextDouble();
			} else if (name.equals("tracker_time")) {
				trackerTime = reader.nextLong();
			} else if (name.equals("system_time")) {
				systemTime = reader.nextLong();
			} else if (name.equals("nano_time")) {
				nanoTime = reader.nextLong();
			} else if (name.equals("line_base_x")) {
				lineBaseX = reader.nextInt();
			} else if (name.equals("line")) {
				line = reader.nextInt();
			} else if (name.equals("col")) {
				col = reader.nextInt();
			} else if (name.equals("hows")) {
				hows = reader.nextString();
			} else if (name.equals("types")) {
				types = reader.nextString();
			} else if (name.equals("fullyQualifiedNames")) {
				fullyQualifiedNames = reader.nextString();
			} else if (name.equals("line_base_y")) {
				lineBaseY = reader.nextInt();
			} else {
				reader.skipValue();
			}
		}
		reader.endObject();
		return new OldRawGaze(file, type, x, y, leftValidity, rightValidity,
				leftPupilDiam, rightPupilDiam, trackerTime, systemTime,
				nanoTime, lineBaseX, line, col, hows, types, fullyQualifiedNames,
				lineBaseY);
	}
	
	@Override
	public void export() throws IOException {
		if (getProcessedGazes() != null) {
			File outFile = new File(fileDir + "/processed-gazes-"
					+ devUsername + "-" + sessionID + ".json");
				if (outFile.exists()) {
					System.out.println("You cannot overwrite this file. If you "
							+ "wish to continue, delete the file " + "manually.");
					return;
				} else {
					System.out.println("Putting files at "
							+ outFile.getAbsolutePath());
					outFile.createNewFile();
					
					//export to new file
					JsonWriter writer = null;
					try {
						writer = new JsonWriter(new FileWriter(outFile));
						writer.setIndent("  ");
						
						//export header
						writer.beginObject()
								.name("environment")
								.beginObject()
									.name("screen_size")
									.beginObject()
										.name("width")
										.value(width)
										.name("height")
										.value(height)
									.endObject()
								.endObject()
								.name("gazes")
								.beginArray();
						
						//export processed gazes
						for (Fixation fixation : getProcessedGazes()) {
							writer.beginObject()
									.name("file")
									.value(fixation.getRawGaze().getFile())
									.name("type")
									.value(fixation.getRawGaze().getType())
									.name("x")
									.value(fixation.getRawGaze().getX())
									.name("y")
									.value(fixation.getRawGaze().getY())
									.name("left_validation")
									.value(fixation.getRawGaze().getLeftValid())
									.name("right_validation")
									.value(fixation.getRawGaze().getRightValid())
									.name("left-pupil-diameter")
									.value(fixation.getRawGaze().getLeftPupilDiam())
									.name("right-pupil-diameter")
									.value(fixation.getRawGaze().getRightPupilDiam())
									.name("tracker_time")
									.value(fixation.getRawGaze().getTrackerTime())
									.name("system_time")
									.value(fixation.getRawGaze().getSystemTime())
									.name("nano_time")
									.value(fixation.getRawGaze().getNanoTime())
									.name("duration")
									.value(fixation.getDuration())
									.name("line_base_x")
									.value(fixation.getRawGaze().getLineBaseX())
									.name("line_base_y")
									.value(fixation.getRawGaze().getLineBaseY())
									.name("line")
									.value(fixation.getRawGaze().getLine())
									.name("col")
									.value(fixation.getRawGaze().getCol())
									.name("hows")
									.value(((OldRawGaze)fixation.getRawGaze()).getHows())
									.name("types")
									.value(((OldRawGaze)fixation.getRawGaze()).getTypes())
									.name("fullyQualifiedNames")
									.value(((OldRawGaze)fixation.getRawGaze()).getFullyQualifiedNames())
								.endObject();
						}
					} catch ( IOException e) {
						throw new IOException("Failed to write processed gazes to file.");
					}
					finally {
						try {
							if ( writer != null) {
								writer.endArray().endObject();
								writer.close( );
								System.out.println("Processed gazes saved.");
							}
						} catch (IOException e) {
							throw new IOException("Failed to write processed gazes to file.");
						}
					}
				}
		}
	}
}
