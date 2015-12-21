package net.kedean.news.dao.impl;

import net.kedean.news.dto.Story;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

/**
 * Storage for stories that are approved and processed.
 *
 * @author kdean
 *
 */
@Repository
public class PublishedStoryRepositoryImpl extends StoryRepositoryWrapperImpl {

    @Value("${stories.lifetimeMillis}")
    protected Integer lifetimeMillis;

    @Override
    public void put(String id, Story story) {
        story.getMetadata().setPublishTime(DateTime.now().getMillis());
        story.getMetadata().setExpirationTime(DateTime.now().plusMillis(this.lifetimeMillis).getMillis());

        super.put(id, story);
    }
}
