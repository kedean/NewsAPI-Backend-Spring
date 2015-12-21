package net.kedean.news.dto;

public class RejectedStory extends Story {

    private static final long serialVersionUID = -3451754681704177495L;
    private String            note;

    public String getNote() {
        return this.note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
