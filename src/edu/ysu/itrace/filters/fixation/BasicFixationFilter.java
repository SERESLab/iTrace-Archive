package edu.ysu.itrace.filters.fixation;


import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.ysu.itrace.filters.IFilter;
import edu.ysu.itrace.filters.RawGaze;

/**
 * Class that defines functionality needed for a post-processing 
 * algorithm to find fixations in raw gaze data
 */
public abstract class BasicFixationFilter implements IFilter {
	private ArrayList<RawGaze> rawGazes;
	private ArrayList<Fixation> processedGazes =
			new ArrayList<Fixation>();
	
	private double[] diffVector;
	private double[] peak;
	private List<Integer> peakIndices;
	private int r = 5; //sliding window
	private double threshold =  35 / (16.6 * r);
	private double radius = 35;

	//Getters
	/**
	 * Return the raw gazes as an array list.
	 */
	public ArrayList<RawGaze> getRawGazes() {
		return rawGazes;
	}
	
	/**
	 * Return the processed gazes as an array list.
	 */
	public ArrayList<Fixation> getProcessedGazes() {
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
	
	/**
	 * Return the Radius/Distance Threshold
	 */
	public double getRadius() {
		return radius;
	}
	
	//Setters
	/**
	 * Set the raw gazes as an array list of gazes.
	 */
	protected void setRawGazes(ArrayList<RawGaze> rawGazes) {
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
	protected void setThreshold(double thresholdPixels) {
		this.threshold = thresholdPixels / (16.6 * r);
	}
	
	/**
	 * Set the distance threshold
	 */
	protected void setRadius(double radius) {
		this.radius = radius;
	}
	
	//Computational/Processing functions
	/**
	 * Interpolate missing data
	 */
	public void interpolate() {
		if (rawGazes != null) {
			RawGaze lastPosition = rawGazes.get(0);
			for (int i = 0; i < rawGazes.size(); i++) {
				if (rawGazes.get(i).getLeftValid() == 1 ||
						rawGazes.get(i).getRightValid() == 1) {
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
					mBefore.x = mBefore.x + rawGazes.get(i-r).getX() / r;
					mBefore.y = mBefore.y + rawGazes.get(i-r).getY() / r;
					mAfter.x = mAfter.x + rawGazes.get(i+r).getX() / r;
					mAfter.y = mAfter.y + rawGazes.get(i+r).getY() / r;
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
		if (peakIndices != null) {
			double shortestDist = 0;
			
			while (shortestDist < radius) {
				processedGazes.clear();
				for (int i = 1; i < peakIndices.size(); i++) {
					processedGazes.add(mergeRawGazes(peakIndices.get(i-1),peakIndices.get(i)));
				}
				
				shortestDist = Integer.MAX_VALUE;
				int index = -1;
				
				for (int j = 1; j < processedGazes.size(); j++) {
					double x =
							processedGazes.get(j).getRawGaze().getX() -
							processedGazes.get(j-1).getRawGaze().getX();
					double y =
							processedGazes.get(j).getRawGaze().getY() -
							processedGazes.get(j-1).getRawGaze().getY();
					double distance = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
					
					if (distance < shortestDist) {
						shortestDist = distance;
						index = j;
					}
				}
				
				if (shortestDist < radius) {
					peakIndices.remove(index);
				}
			}
		}
	}
	
	/**
	 * Merge raw gazes together based on peak indices
	 */
	public Fixation mergeRawGazes(int iStart, int iEnd) {
		double[] x = new double[iEnd-iStart+1];
		double[] y = new double[iEnd-iStart+1];
		double leftPupilDiam = 0;
		double rightPupilDiam = 0;
		
		for (int i = iStart; i <= iEnd; i++) {
			x[i-iStart] = rawGazes.get(i).getX();
			y[i-iStart] = rawGazes.get(i).getY();
			
			leftPupilDiam += rawGazes.get(i).getLeftPupilDiam();
			rightPupilDiam += rawGazes.get(i).getRightPupilDiam();
			
		}
		
		//Compute the left/right pupil diameters by averaging across interval
		leftPupilDiam = leftPupilDiam / (iEnd-iStart+1);
		rightPupilDiam = rightPupilDiam / (iEnd-iStart+1);
		
		//Compute the median of all of the x and y values across interval
		Arrays.sort(x);
		Arrays.sort(y);
		double medianX;
		double medianY;
		if (x.length % 2 == 0) {
			medianX = (x[x.length/2] + x[x.length/2 - 1])/2;
		} else {
			medianX = x[x.length/2];
		}
		if (y.length % 2 == 0) {
			medianY = (y[y.length/2] + y[y.length/2 - 1])/2;
		} else {
			medianY = y[y.length/2];
		}

		//Compute the fixation duration
		long duration = rawGazes.get(iEnd).getSystemTime() -
				rawGazes.get(iStart).getSystemTime();
		
		//Create the new processed fixation
		RawGaze rawGaze = rawGazes.get(iStart);
		RawGaze processedGaze = new RawGaze(rawGaze.getFile(), rawGaze.getType(),
				medianX, medianY, 1, 1, leftPupilDiam, rightPupilDiam,
				rawGaze.getTrackerTime(), rawGaze.getSystemTime(),
				rawGaze.getNanoTime(), rawGaze.getLineBaseX(), rawGaze.getLine(),
				rawGaze.getCol(), rawGaze.getHows(), rawGaze.getTypes(),
				rawGaze.getFullyQualifiedNames(), rawGaze.getLineBaseY());
		Fixation fixation = new Fixation(processedGaze, duration);
		
		return fixation;
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
	
}
