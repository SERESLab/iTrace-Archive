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

import edu.ysu.itrace.filters.SourceCodeEntity;
import edu.ysu.itrace.filters.NewRawGaze;
import edu.ysu.itrace.filters.RawGaze;

public class JSONBasicFixationFilter extends BasicFixationFilter {
	//log file header variables
	private int width;
	private int height;
	
	private String fileDir;
	private String devUsername;
	private String sessionID;
	
	private final String filterName = "JSON Fixation Filter";
	
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
			sessionID = parts[3] + "-" + parts[4] + "-" + parts[5].split(Pattern.quote("."))[0];
				
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
	
	public RawGaze getRawGaze(JsonReader reader) throws IOException {
		String file = null;
		String type = null;
		double x = -1;
		double y = -1;
		double leftValidity = -1;
		double rightValidity = -1;
		double leftPupilDiam = -1;
		double rightPupilDiam = -1;
		String timeStamp = null;
		long sessionTime = -1;
		long trackerTime = -1;
		long systemTime = -1;
		long nanoTime = -1;
		String path = new String();
		int lineHeight = -1;
		int fontHeight = -1;
		int lineBaseX = -1;
		int line = -1;
		int col = -1;
		int lineBaseY = -1;
		ArrayList<SourceCodeEntity> sces = new ArrayList<SourceCodeEntity>();
		
		reader.beginObject();
		while (reader.hasNext()) {
			String name = reader.nextName();
			if (name.equals("name")) {
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
			} else if (name.equals("left_pupil_diameter")) {
				leftPupilDiam = reader.nextDouble();
			} else if (name.equals("right_pupil_diameter")) {
				rightPupilDiam = reader.nextDouble();
			} else if (name.equals("timestamp")) {
				timeStamp = reader.nextString();
			} else if (name.equals("session_time")) {
				sessionTime = reader.nextLong();
			} else if (name.equals("tracker_time")) {
				trackerTime = reader.nextLong();
			} else if (name.equals("system_time")) {
				systemTime = reader.nextLong();
			} else if (name.equals("nano_time")) {
				nanoTime = reader.nextLong();
			} else if (name.equals("path")) {
				path = reader.nextString();
			} else if (name.equals("line_height")) {
				lineHeight = reader.nextInt();
			} else if (name.equals("font_height")) {
				fontHeight = reader.nextInt();
			} else if (name.equals("line_base_x")) {
				lineBaseX = reader.nextInt();
			} else if (name.equals("line")) {
				line = reader.nextInt();
			} else if (name.equals("col")) {
				col = reader.nextInt();
			} else if (name.equals("line_base_y")) {
				lineBaseY = reader.nextInt();
			} else if (name.equals("sces")) {
				reader.beginArray();
				while (reader.hasNext()) {
					sces.add(getSce(reader));
				}
				reader.endArray();
			} else {
				reader.skipValue();
			}
		}
		reader.endObject();
		return new NewRawGaze(file, type, x, y, leftValidity, rightValidity,
				leftPupilDiam, rightPupilDiam, timeStamp, sessionTime,
				trackerTime, systemTime, nanoTime, path, lineHeight,
				fontHeight, lineBaseX, line, col,
				lineBaseY, sces);
	}
	
	public SourceCodeEntity getSce(JsonReader reader) throws IOException {
		String name = new String();
		String type = new String();
		String how = new String();
		int length = -1;
		int startLine = -1;
		int endLine = -1;
		int startCol = -1;
		int endCol = -1;
		
		reader.beginObject();
		while(reader.hasNext()) {
			String Name = reader.nextName();
			if (Name.equals("name")) {
				name = reader.nextString();
			} else if (Name.equals("type")) {
				type = reader.nextString();
			} else if (Name.equals("how")) {
				how = reader.nextString();
			} else if (Name.equals("total_length")) {
				length = reader.nextInt();
			} else if (Name.equals("start_line")) {
				startLine = reader.nextInt();
			} else if (Name.equals("end_line")) {
				endLine = reader.nextInt();
			} else if (Name.equals("start_col")) {
				startCol = reader.nextInt();
			} else if (Name.equals("end_col")) {
				endCol = reader.nextInt();
			} else {
				reader.skipValue();
			}
		}
		reader.endObject();
		
		return new SourceCodeEntity(name, type, how, length,
				startLine, endLine, startCol, endCol);
	}
	
	@Override
	public void export() throws IOException {
		if (getProcessedGazes() != null && getRawGazeFixations() != null) {
			//export raw gazes with associated fixations
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
						for (final NewRawGazeFixation rawGaze : getRawGazeFixations()) {
							writer.beginObject()
									.name("file")
									.value(rawGaze.getFile())
									.name("type")
									.value(rawGaze.getType())
									.name("x")
									.value(rawGaze.getX())
									.name("y")
									.value(rawGaze.getY())
									.name("left_validation")
									.value(rawGaze.getLeftValid())
									.name("right_validation")
									.value(rawGaze.getRightValid())
									.name("left_pupil_diameter")
									.value(rawGaze.getLeftPupilDiam())
									.name("right_pupil_diameter")
									.value(rawGaze.getRightPupilDiam())
									.name("timestamp")
									.value(rawGaze.getTimeStamp())
									.name("session_time")
									.value(rawGaze.getSessionTime())
									.name("tracker_time")
									.value(rawGaze.getTrackerTime())
									.name("system_time")
									.value(rawGaze.getSystemTime())
									.name("nano_time")
									.value(rawGaze.getNanoTime())
									.name("path")
									.value(rawGaze.getPath())
									.name("line_height")
									.value(rawGaze.getLineHeight())
									.name("font_height")
									.value(rawGaze.getFontHeight())
									.name("line")
									.value(rawGaze.getLine())
									.name("col")
									.value(rawGaze.getCol())
									.name("line_base_x")
									.value(rawGaze.getLineBaseX())
									.name("line_base_y")
									.value(rawGaze.getLineBaseY())
									.name("fix_index")
									.value(rawGaze.getFixIndex())
									.name("fix_x")
									.value(rawGaze.getFixX())
									.name("fix_y")
									.value(rawGaze.getFixY())
									.name("duration")
									.value(rawGaze.getDuration())
									.name("sces")
									.beginArray();
							
							for (final SourceCodeEntity sce : rawGaze.getSces()) {
								writer.beginObject()
										.name("name")
										.value(sce.getName())
										.name("type")
										.value(sce.getType())
										.name("how")
										.value(sce.getHow())
										.name("total_length")
										.value(sce.getTotalLength())
										.name("start_line")
										.value(sce.getStartLine())
										.name("end_line")
										.value(sce.getEndLine())
										.name("start_col")
										.value(sce.getStartCol())
										.name("end_col")
										.value(sce.getEndCol())
										.endObject();
							}
								writer.endArray().endObject();
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
			
			//export fixations
			outFile = new File(fileDir + "/processed-fixations-"
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
								.name("fix_stats")
								.beginObject()
									.name("discarded_fix")
									.value(getDiscardedFix())
									.name("total_fix")
									.value(getProcessedGazes().size())
									.name("filter")
									.value(filterName)
									.name("sliding_window")
									.value(getSlidingWindow())
									.name("peak_threshold")
									.value(getThreshold())
									.name("radius")
									.value(getRadius())
									.name("duration_threshold")
									.value(getDurationThresh())
								.endObject()
								.name("fixations")
								.beginArray();
						
						//export processed fixations
						for (final Fixation fixation : getProcessedGazes()) {
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
									.name("left_pupil_diameter")
									.value(fixation.getRawGaze().getLeftPupilDiam())
									.name("right_pupil_diameter")
									.value(fixation.getRawGaze().getRightPupilDiam())
									.name("timestamp")
									.value(((NewRawGaze)fixation.getRawGaze()).getTimeStamp())
									.name("session_time")
									.value(((NewRawGaze)fixation.getRawGaze()).getSessionTime())
									.name("tracker_time")
									.value(fixation.getRawGaze().getTrackerTime())
									.name("system_time")
									.value(fixation.getRawGaze().getSystemTime())
									.name("nano_time")
									.value(fixation.getRawGaze().getNanoTime())
									.name("path")
									.value(((NewRawGaze)fixation.getRawGaze()).getPath())
									.name("line_height")
									.value(((NewRawGaze)fixation.getRawGaze()).getLineHeight())
									.name("font_height")
									.value(((NewRawGaze)fixation.getRawGaze()).getFontHeight())
									.name("line")
									.value(fixation.getRawGaze().getLine())
									.name("col")
									.value(fixation.getRawGaze().getCol())
									.name("line_base_x")
									.value(fixation.getRawGaze().getLineBaseX())
									.name("line_base_y")
									.value(fixation.getRawGaze().getLineBaseY())
									.name("fix_index")
									.value(fixation.getFixIndex())
									.name("duration")
									.value(fixation.getDuration())
									.name("sces")
									.beginArray();
							
							for (final SourceCodeEntity sce : ((NewRawGaze)fixation.getRawGaze())
									.getSces()) {
								writer.beginObject()
										.name("name")
										.value(sce.getName())
										.name("type")
										.value(sce.getType())
										.name("how")
										.value(sce.getHow())
										.name("total_length")
										.value(sce.getTotalLength())
										.name("start_line")
										.value(sce.getStartLine())
										.name("end_line")
										.value(sce.getEndLine())
										.name("start_col")
										.value(sce.getStartCol())
										.name("end_col")
										.value(sce.getEndCol())
										.endObject();
							}
								writer.endArray().endObject();
						}
					} catch ( IOException e) {
						throw new IOException("Failed to write processed fixations to file.");
					}
					finally {
						try {
							if ( writer != null) {
								writer.endArray().endObject();
								writer.close( );
								System.out.println("Processed fixations saved.");
							}
						} catch (IOException e) {
							throw new IOException("Failed to write processed fixations to file.");
						}
					}
				}
		}
	}
}
