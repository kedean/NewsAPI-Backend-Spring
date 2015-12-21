package net.kedean.news.dto;

import java.io.Serializable;

public class IngestionMetadata implements Serializable {

    private static final long serialVersionUID = 2529485644618635351L;

    public enum DateType {
        INGESTED, PUBLISHED, REJECTED, EXPIRED;
    };

    private Long ingestionTime, publishTime, rejectionTime, expirationTime;

    public Long getIngestionTime() {
        return this.ingestionTime;
    }

    public void setIngestionTime(Long ingestionTime) {
        this.ingestionTime = ingestionTime;
    }

    public Long getPublishTime() {
        return this.publishTime;
    }

    public void setPublishTime(Long publishTime) {
        this.publishTime = publishTime;
    }

    public Long getRejectionTime() {
        return this.rejectionTime;
    }

    public void setRejectionTime(Long rejectionTime) {
        this.rejectionTime = rejectionTime;
    }

    public Long getExpirationTime() {
        return this.expirationTime;
    }

    public void setExpirationTime(Long expirationTime) {
        this.expirationTime = expirationTime;
    }
}
