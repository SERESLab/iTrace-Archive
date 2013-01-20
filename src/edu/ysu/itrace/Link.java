package edu.ysu.itrace;

public class Link {
	Aoi startAoi;
	Aoi endAoi;
	Gaze gaze;
	Boolean userConfirmed = false;
	
	public Link (Aoi startAoi, Aoi endAoi, Gaze gaze){
		this.startAoi = startAoi;
		this.endAoi = endAoi;
		this.gaze = gaze;
	}
	
	public void confirmLink(Boolean confirmed) {
		userConfirmed = confirmed;
	}
}
