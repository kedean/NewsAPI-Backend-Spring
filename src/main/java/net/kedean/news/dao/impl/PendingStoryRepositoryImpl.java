package net.kedean.news.dao.impl;

import net.kedean.news.dto.IngestionMetadata;
import net.kedean.news.dto.Story;

import org.joda.time.DateTime;
import org.springframework.stereotype.Repository;

/**
 * Storage for stories waiting on processing
 *
 * @author kdean
 *
 */
@Repository
public class PendingStoryRepositoryImpl extends StoryRepositoryWrapperImpl {

    @Override
    public void put(String id, Story story) {
        IngestionMetadata metadata = new IngestionMetadata();
        metadata.setIngestionTime(DateTime.now().getMillis());
        story.setMetadata(metadata);

        super.put(id, story);
    }
}
