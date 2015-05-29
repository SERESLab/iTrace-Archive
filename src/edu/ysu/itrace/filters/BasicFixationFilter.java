package edu.ysu.itrace.filters;


import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import edu.ysu.itrace.gaze.IGazeResponse;

/**
 * Class that defines functionality needed for a post-processing 
 * algorithm to find fixations in raw gaze data
 */
public abstract class BasicFixationFilter implements IFilter {
	private ArrayList<IGazeResponse> rawGazes;
	private ArrayList<IGazeResponse> processedGazes;
	private double[] diffVector;
	private double[] peak;
	private List<Integer> peakIndices;
	private int r = 4; //sliding window
	private double threshold = 
	private String filterName = "Basic Fixation Filter";

	//Getters
	/**
	 * Return the raw gazes as an array list.
	 */
	public ArrayList<IGazeResponse> getRawGazes() {
		return rawGazes;
	}
	
	/**
	 * Return the processed gazes as an array list.
	 */
	public ArrayList<IGazeResponse> getProcessedGazes() {
		return processedGazes;
	}
	
	/**
	 * Return the sliding window value
	 */
	public int getSlidingWindow() {
		return r;
	}
	
	/**
	 * Return the threshold value for removing peaks
	 */
	public double getThreshold() {
		return threshold;
	}
	
	//Setters
	/**
	 * Set the raw gazes as an array list of gazes.
	 */
	protected void setRawGazes(ArrayList<IGazeResponse> rawGazes) {
		this.rawGazes = rawGazes;
	}
	
	/**
	 * Set the sliding window for the fixation calculation
	 */
	protected void setSlidingWindow(int r) {
		this.r = r;
	}
	
	/**
	 * Set the threshold for removing peaks
	 */
	protected void setThreshold(double threshold) {
		this.threshold = threshold;
	}
	
	//Computational/Processing functions
	/**
	 * Interpolate missing data
	 */
	public void interpolate() {
		if (rawGazes != null) {
			IGazeResponse lastPosition = rawGazes.get(0);
			for (int i = 0; i < rawGazes.size(); i++) {
				if (rawGazes.get(i).getGaze().getLeftValidity() == 1 ||
						rawGazes.get(i).getGaze().getRightValidity() == 1) {
					lastPosition = rawGazes.get(i);
				} else {
					rawGazes.set(i,lastPosition);
				}
			}
		} 
	}
	
	/**
	 * Calculate the difference vector
	 */
	public void diffVector() {
		if (rawGazes != null) {
			diffVector = new double[rawGazes.size()];
			Point2D.Double mBefore = new Point2D.Double();
			Point2D.Double mAfter = new Point2D.Double();
			
			for (int k = 0; k < r; k++) {
				diffVector[k] = 0;
			}
			for (int l = rawGazes.size()-1; l > rawGazes.size()-r-1; l--) {
				diffVector[l] = 0;
			}
			
			for (int i = r; i < rawGazes.size()-r; i++) {
				mBefore.setLocation(0,0);
				mAfter.setLocation(0,0);
				for (int j = 1; j <= r; j++) {
					mBefore.x = mBefore.x + rawGazes.get(i-r).getGaze().getX() / r;
					mBefore.y = mBefore.y + rawGazes.get(i-r).getGaze().getY() / r;
					mAfter.x = mAfter.x + rawGazes.get(i+r).getGaze().getX() / r;
					mAfter.y = mAfter.y + rawGazes.get(i+r).getGaze().getY() / r;
				}
				diffVector[i] = Math.sqrt(Math.pow(mAfter.x-mBefore.x, 2) +
						Math.pow(mAfter.y-mBefore.y,2));
			}
		}
	}
	
	/**
	 * Find peaks in difference vector
	 */
	public void peaks() {
		if (diffVector != null) {
			peak = new double[rawGazes.size()];
			
			for (int i = 0; i < rawGazes.size(); i++) {
				peak[i] = 0;
			}
			
			for (int j = 1; j < rawGazes.size()-1; j++) {
				if (diffVector[j] > diffVector[j-1] &&
						diffVector[j] > diffVector[j+1]) {
					peak[j] = diffVector[j];
				}
			}
		}
	}
	
	/**
	 * Remove peaks that are too close to each other in the time domain
	 */
	public void processPeaks() {
		if (peak != null) {
			for (int i = r; i < rawGazes.size()-r; i++) {
				if (peak[i] != 0) {
					for (int j = i-r; j < i; j++) {
						if (peak[j] < peak[i]) {
							peak[j] = 0;
						}
						for (int k = i+1; k <= i+r; k++) {
							if (peak[j] < peak[i]) {
								peak[j] = 0;
							}
						}
					}
				}
			}
		}
	}
	
	/**
	 * Create a list with peak indices
	 */
	public void peakIndices() {
		if (peak != null) {
			peakIndices = new ArrayList<>();
			
			for (int i = 0; i < rawGazes.size(); i++) {
				if (peak[i] >= threshold) {
					peakIndices.add(i);
				}
			}
		}
	}
	
	/**
	 * Estimate spatial position of fixations
	 */
	public void spatialPos() {
		
	}
	
	//Overridden function
	@Override
	public void process() {
		interpolate();
		diffVector();
		peaks();
		processPeaks();
		peakIndices();
		spatialPos();
	}
	
	@Override
	public String getFilterName() {
		return filterName;
	}
	
	@Override
	public void filterUI() {
		
	}
}
