package net.kedean.news.dto;

import java.io.Serializable;

public class Story implements Serializable {

    private static final long serialVersionUID = -3192183147583119772L;
    private String            headline;
    private String            link;
    private IngestionMetadata metadata;

    public String getHeadline() {
        return this.headline;
    }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public String getLink() {
        return this.link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public IngestionMetadata getMetadata() {
        return this.metadata;
    }

    public void setMetadata(IngestionMetadata metadata) {
        this.metadata = metadata;
    }
}
