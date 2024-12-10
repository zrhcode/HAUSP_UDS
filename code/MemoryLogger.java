/**
 * @ClassName: MemoryLogger
 * @Author: zrh
 * @Description: This class is used to record the maximum memory usage of
 * an algorithm during a given execution. It is implemented using the singleton design pattern.
 */

public class MemoryLogger {

    // The only instance of this class (singleton pattern)
    private static final MemoryLogger instance = new MemoryLogger();

    // Variable to store the maximum memory usage
    private double maxMemory = 0;

    /**
     * Method to obtain the only instance of this class
     * @return instance of MemoryLogger
     */
    public static MemoryLogger getInstance(){
        return instance;
    }

    /**
     * Gets the maximum amount of memory used until now
     * @return the maximum memory usage in megabytes as a double value
     */
    public double getMaxMemoryUsage() {
        return maxMemory;
    }

    /**
     * Resets the maximum amount of memory recorded.
     */
    public void reset(){
        maxMemory = 0;
    }

    /**
     * Checks the current memory usage and records it if it is higher
     * than the amount of memory previously recorded.
     */
    public void checkMemory() {
        Runtime runtime = Runtime.getRuntime();
        double currentMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024d / 1024d;
        if (currentMemory > maxMemory) {
            maxMemory = currentMemory;
        }
    }
}
