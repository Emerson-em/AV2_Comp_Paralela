package analysis;

public class SortResult {
    private String algorithm;
    private String version;
    private int datasetSize;
    private String datasetType;
    private int numThreads;
    private double executionTime;
    private boolean isSorted;
    private long timestamp;

    public SortResult(String algorithm, String version, int datasetSize,
                      String datasetType, int numThreads, double executionTime,
                      boolean isSorted, long timestamp) {
        this.algorithm = algorithm;
        this.version = version;
        this.datasetSize = datasetSize;
        this.datasetType = datasetType;
        this.numThreads = numThreads;
        this.executionTime = executionTime;
        this.isSorted = isSorted;
        this.timestamp = timestamp;
    }

    // Getters
    public String getAlgorithm() { return algorithm; }
    public String getVersion() { return version; }
    public int getDatasetSize() { return datasetSize; }
    public String getDatasetType() { return datasetType; }
    public int getNumThreads() { return numThreads; }
    public double getExecutionTime() { return executionTime; }
    public boolean isSorted() { return isSorted; }
    public long getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return String.format("%s,%s,%d,%s,%d,%.6f,%b,%d",
                algorithm, version, datasetSize, datasetType, numThreads,
                executionTime, isSorted, timestamp);
    }

    public static String getCSVHeader() {
        return "algorithm,version,dataset_size,dataset_type,num_threads,execution_time,is_sorted,timestamp";
    }
}