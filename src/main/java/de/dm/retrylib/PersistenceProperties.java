package de.dm.retrylib;

public class PersistenceProperties {

    private static final Long DEFAULT_MAX_ENTRIES = 1000000L;
    private static final Double DEFAULT_AVERAGE_VALUE_SIZE = 600.0;
    private static final String DEFAULT_FILE_PATH = System.getProperty("java.io.tmpdir");
    private static final String DEFAULT_FILE_NAME = "retryChronicleMap.dat";

    private Long maxEntries = DEFAULT_MAX_ENTRIES;
    private Double averageValueSize = DEFAULT_AVERAGE_VALUE_SIZE;
    private String filePath = DEFAULT_FILE_PATH;
    private String fileName = DEFAULT_FILE_NAME;

    public Long getMaxEntries() {
        return maxEntries;
    }

    public void setMaxEntries(Long maxEntries) {
        this.maxEntries = maxEntries;
    }

    public Double getAverageValueSize() {
        return averageValueSize;
    }

    public void setAverageValueSize(Double averageValueSize) {
        this.averageValueSize = averageValueSize;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
