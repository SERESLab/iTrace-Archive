package edu.ysu.itrace.filters;

import java.io.IOException;

/**
 * Interface for defining basic I/O for filters used on exported gaze data.
 */
public interface IFilter {

	/**
	 * Name of filter suitable for display to user.
	 */
	public String getFilterName();
	
	/**
	 * UI to select files for post-processing.
	 */
	public void filterUI();
	
	/**
	 * Read in selected files.
	 */
	public void init() throws IOException;
	
	/**
	 * Process the selected files based on specific filter type.
	 */
	public void process();
	
	/**
	 * Export processed files.
	 */
	public void export();
	
	/**
	 * Free resources.
	 */
	public void dispose() throws IOException;
}
