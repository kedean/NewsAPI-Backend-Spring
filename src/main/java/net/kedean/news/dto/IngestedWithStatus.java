package net.kedean.news.dto;

import net.kedean.news.dto.IngestionStatus.Status;

public class IngestedWithStatus extends IngestedStory {

    private Status status;

    public IngestedWithStatus(String id, Story story, Status status) {
        super(id, story);
        this.status = status;
    }

    public static IngestedWithStatus Pending(IngestedStory story) {
        return new IngestedWithStatus(story.getId(), story.getDetails(), Status.PENDING);
    }

    public static IngestedWithStatus Published(IngestedStory story) {
        return new IngestedWithStatus(story.getId(), story.getDetails(), Status.PUBLISHED);
    }

    public static IngestedWithStatus Rejected(IngestedStory story) {
        return new IngestedWithStatus(story.getId(), story.getDetails(), Status.REJECTED);
    }

    public static IngestedWithStatus NotFound(IngestedStory story) {
        return new IngestedWithStatus(story.getId(), story.getDetails(), Status.NOT_FOUND);
    }

    public Status getStatus() {
        return this.status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
