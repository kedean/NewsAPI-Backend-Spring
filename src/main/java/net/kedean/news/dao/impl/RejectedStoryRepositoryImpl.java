package net.kedean.news.dao.impl;

import net.kedean.news.dto.IngestionMetadata;
import net.kedean.news.dto.Story;

import org.joda.time.DateTime;
import org.springframework.stereotype.Repository;

/**
 * Storage exclusively for rejected stories.
 *
 * @author kdean
 *
 */
@Repository
public class RejectedStoryRepositoryImpl extends StoryRepositoryWrapperImpl {

    @Override
    public void put(String id, Story story) {
        if (story == null) {
            story = new Story();
        }
        if (story.getMetadata() == null) {
            story.setMetadata(new IngestionMetadata());
        }
        story.getMetadata().setRejectionTime(DateTime.now().getMillis());

        super.put(id, story);
    }
}
