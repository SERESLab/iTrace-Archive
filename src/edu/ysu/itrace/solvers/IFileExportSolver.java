package edu.ysu.itrace.solvers;

/**
 * Defines an interface for solvers that dump data to files.
 */
public interface IFileExportSolver extends ISolver {
    /**
     * Get the filename that would be used.
     * @return a string containing the export path
     */
    public String getFilename();
}
