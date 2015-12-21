package net.kedean.news.dto;

public class PublishedStory extends Story {

    private byte[] preview;

    public byte[] getPreview() {
        return this.preview;
    }

    public void setPreview(byte[] preview) {
        this.preview = preview;
    }
}
