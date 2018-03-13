package de.dm.retrylib;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "retrylib")
public class RetrylibProperties {

    private PersistenceProperties persistence = new PersistenceProperties();

    public PersistenceProperties getPersistence() {
        return persistence;
    }

    public void setPersistence(PersistenceProperties persistence) {
        this.persistence = persistence;
    }

    public static class PersistenceProperties {

        private Long maxEntries = 1000000L;

        private Double averageValueSize = 600.0;

        private String filePath;

        private String fileName;


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


}
