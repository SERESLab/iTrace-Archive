package edu.ysu.itrace.solvers;

/**
 * Defines an interface for solvers that dump data to files.
 */
public interface IFileExportSolver extends ISolver {
    /**
     * Get the pattern used to name exported files.
     * @return a string to be passed to SimpleDateFormat
     */
    public String getFilenamePattern();

    /**
     * Set the pattern used to name exported files.
     * @param a string to be passed to SimpleDateFormat
     */
    public void setFilenamePattern(String filenamePattern);

    /**
     * Get the filename that would be used, given the current pattern.
     * @return a string containing the export path
     */
    public String getFilename();
}
