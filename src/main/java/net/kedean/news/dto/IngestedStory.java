package net.kedean.news.dto;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document
public class IngestedStory implements Serializable {

    private static final long serialVersionUID = -549892781599462572L;

    @Id
    private String            id;

    @Field
    private Story             details;

    public IngestedStory(String id, Story details) {
        this.setId(id);
        this.setDetails(details);
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Story getDetails() {
        return this.details;
    }

    public void setDetails(Story details) {
        this.details = details;
    }
}
