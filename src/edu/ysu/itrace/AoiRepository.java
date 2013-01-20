package edu.ysu.itrace;

import java.util.ArrayList;

public class AoiRepository {
	private static ArrayList<Aoi> aois = new ArrayList<Aoi>();
	
	public static void addAoi (Aoi aoi) {
		aois.add(aoi);
	}
	
	public static void removeAoi (Aoi aoi) {
		aois.remove(aoi);
	}
}
