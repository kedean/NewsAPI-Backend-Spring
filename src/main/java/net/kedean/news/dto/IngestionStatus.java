package net.kedean.news.dto;

public class IngestionStatus {

    public enum Status {
        PUBLISHED, PENDING, REJECTED, NOT_FOUND;
    }

    private String id;
    private Status status;

    public IngestionStatus(String storyId, Status status) {
        this.id = storyId;
        this.status = status;
    }

    public static IngestionStatus Pending(String storyId) {
        return new IngestionStatus(storyId, Status.PENDING);
    }

    public static IngestionStatus Published(String storyId) {
        return new IngestionStatus(storyId, Status.PUBLISHED);
    }

    public static IngestionStatus Rejected(String storyId) {
        return new IngestionStatus(storyId, Status.REJECTED);
    }

    public static IngestionStatus NotFound(String storyId) {
        return new IngestionStatus(storyId, Status.NOT_FOUND);
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Status getStatus() {
        return this.status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
