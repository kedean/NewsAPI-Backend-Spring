package net.kedean.news.dao;

import java.util.Collection;

import net.kedean.news.dto.IngestedStory;
import net.kedean.news.dto.IngestionMetadata;
import net.kedean.news.dto.Story;

/**
 * Storage mechanism for stories
 * 
 * @author kdean
 *
 */
public interface StoryRepository {

    public void put(String id, Story story);

    public void put(IngestedStory story);

    public void delete(IngestedStory story);

    public Collection<IngestedStory> all();

    public Collection<IngestedStory> queryByDatesBefore(long compareTime, IngestionMetadata.DateType type);

    public Collection<IngestedStory> queryByDatesAfter(long compareTime, IngestionMetadata.DateType type);

    public IngestedStory queryById(String id);

    public boolean contains(String id);
}
