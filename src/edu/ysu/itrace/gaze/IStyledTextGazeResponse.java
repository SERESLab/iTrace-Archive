package edu.ysu.itrace.gaze;

import edu.ysu.itrace.AstManager.SourceCodeEntity;

/**
 * Defines an interface for gazes falling on StyledText widgets 
 */
public interface IStyledTextGazeResponse extends IGazeResponse {
	/**
	 * Returns a list of SCEs, sorted from most to least specific, under the gaze
	 */
    public SourceCodeEntity[] getSCEs();

    /**
     * Return the OS dependent path of the file in the editor
     */
    public String getPath();

    /**
     * Return the height (in pixels) of lines of text
     */
    public int getLineHeight();

    /**
     * Return the font size
     */
    public int getFontHeight();

    /**
     * Return the line where the gaze fell
     */
    public int getLine();

    /**
     * Return the column where the gaze fell
     */
    public int getCol();

    /**
     * Return the x position of the first character on line
     */
    public int getLineBaseX();

    /**
     * Return the y position of the first character on the line
     */
    public int getLineBaseY();
    
    public int getRelativeX();
    
    public int getRelativeY();
    
    public int getCharIndex();
}
