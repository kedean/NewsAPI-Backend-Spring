package net.kedean.news.dao.impl;

import java.util.Collection;

import net.kedean.news.dao.StoryRepository;
import net.kedean.news.dto.IngestedStory;
import net.kedean.news.dto.IngestionMetadata;
import net.kedean.news.dto.Story;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

/**
 * MongoDB implementation of the story repository
 *
 * @author kdean
 *
 */
@Repository
@Scope("prototype")
public class StoryRepositoryMongoImpl implements StoryRepository {

    @Autowired
    protected MongoTemplate mongo;

    private String          collectionName;

    public String getCollectionName() {
        return this.collectionName;
    }

    public void setCollectionName(String collectionName) {
        this.collectionName = collectionName;
    }

    @Override
    public void put(String id, Story story) {
        this.put(new IngestedStory(id, story));
    }

    @Override
    public void put(IngestedStory story) {
        this.mongo.save(story, this.collectionName);
    }

    @Override
    public void delete(IngestedStory story) {
        this.mongo.remove(story, this.collectionName);
    }

    @Override
    public Collection<IngestedStory> all() {
        return this.mongo.findAll(IngestedStory.class, this.collectionName);
    }

    private String getFieldNameFor(IngestionMetadata.DateType type) {
        switch (type) {
        case EXPIRED:
            return "details.metadata.expirationTime";
        case INGESTED:
            return "details.metadata.ingestionTime";
        case PUBLISHED:
            return "details.metadata.publishTime";
        case REJECTED:
            return "details.metadata.rejectionTime";
        default:
            throw new IllegalArgumentException("Invalid date type: " + type);
        }
    }

    @Override
    public Collection<IngestedStory> queryByDatesBefore(long compareTime, IngestionMetadata.DateType type) {
        Query expiredQuery = new Query();

        expiredQuery.addCriteria(Criteria.where(this.getFieldNameFor(type)).lt(compareTime));
        return this.mongo.find(expiredQuery, IngestedStory.class, this.collectionName);
    }

    @Override
    public Collection<IngestedStory> queryByDatesAfter(long compareTime, IngestionMetadata.DateType type) {
        Query expiredQuery = new Query();

        expiredQuery.addCriteria(Criteria.where(this.getFieldNameFor(type)).gt(compareTime));
        return this.mongo.find(expiredQuery, IngestedStory.class, this.collectionName);
    }

    @Override
    public IngestedStory queryById(String id) {
        return this.mongo.findById(id, IngestedStory.class, this.collectionName);
    }

    @Override
    public boolean contains(String id) {
        Query query = new Query();
        query.addCriteria(Criteria.where("id").is(id));
        return this.mongo.exists(query, IngestedStory.class, this.collectionName);
    }

}
