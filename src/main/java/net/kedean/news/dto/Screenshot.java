package net.kedean.news.dto;

import java.io.Serializable;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document
public class Screenshot implements Serializable {

    private static final long serialVersionUID = 7276803949064976476L;

    @Id
    String                    id;

    @Field
    byte[]                    data;

    public Screenshot(String id, byte[] data) {
        this.id = id;
        this.data = data;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public byte[] getData() {
        return this.data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
