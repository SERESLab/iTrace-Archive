package edu.ysu.itrace;

public class Link {
	Gaze startGaze;
	Gaze endGaze;
	int score;
	int count;
	long timespan = 0;
	Boolean userConfirmed;
	
	public Link (Gaze startGaze, Gaze endGaze){
		this.startGaze = startGaze;
		this.endGaze = endGaze;
		this.userConfirmed = false;
		this.score = 0;
		this.count = 1;
		
		calculateTimespan(startGaze, endGaze);
	}
	
	public void addOccurance(Gaze startGaze, Gaze endGaze){
		this.count++;
		
		// recalculate timespan
		calculateTimespan(startGaze, endGaze);
	}
	
	private void calculateTimespan(Gaze startGaze, Gaze endGaze){
		// get time in milliseconds between gazes
		long difference = endGaze.getTimeStamp().getTime() - startGaze.getTimeStamp().getTime();
		
		if (timespan != 0){
			// average the current timespan and the new one together
			timespan = (timespan + difference) / 2;
		} else {
			timespan = difference;
		}
	}
	
	public void confirmLink(Boolean confirmed) {
		userConfirmed = confirmed;
	}
}
