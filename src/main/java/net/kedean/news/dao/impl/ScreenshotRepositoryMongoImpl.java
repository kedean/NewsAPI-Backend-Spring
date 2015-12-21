package net.kedean.news.dao.impl;

import net.kedean.news.dao.ScreenshotRepository;
import net.kedean.news.dto.Screenshot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Scope("prototype")
public class ScreenshotRepositoryMongoImpl implements ScreenshotRepository {

    @Autowired
    protected MongoTemplate mongo;

    @Override
    public void put(Screenshot screenshot) {
        this.mongo.insert(screenshot);
    }

    @Override
    public Screenshot queryById(String id) {
        return this.mongo.findById(id, Screenshot.class);
    }

}
