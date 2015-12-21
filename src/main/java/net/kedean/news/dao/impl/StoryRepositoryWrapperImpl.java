package net.kedean.news.dao.impl;

import java.util.Collection;

import net.kedean.news.dao.StoryRepository;
import net.kedean.news.dto.IngestedStory;
import net.kedean.news.dto.IngestionMetadata.DateType;
import net.kedean.news.dto.Story;

/**
 * A story repository that relies on a separate injected repository for storage. This allows for configurations where the storage mechanism can be swapped out for another one.
 *
 * @author kdean
 *
 */
public abstract class StoryRepositoryWrapperImpl implements StoryRepository {

    protected StoryRepository internalRepo;

    public StoryRepository getInternalRepo() {
        return this.internalRepo;
    }

    public void setInternalRepo(StoryRepository internalRepo) {
        this.internalRepo = internalRepo;
    }

    @Override
    public void put(String id, Story story) {
        this.internalRepo.put(id, story);
    }

    @Override
    public void put(IngestedStory story) {
        this.internalRepo.put(story);
    }

    @Override
    public void delete(IngestedStory story) {
        this.internalRepo.delete(story);
    }

    @Override
    public Collection<IngestedStory> all() {
        return this.internalRepo.all();
    }

    @Override
    public Collection<IngestedStory> queryByDatesBefore(long compareTime, DateType type) {
        return this.internalRepo.queryByDatesBefore(compareTime, type);
    }

    @Override
    public Collection<IngestedStory> queryByDatesAfter(long compareTime, DateType type) {
        return this.internalRepo.queryByDatesAfter(compareTime, type);
    }

    @Override
    public IngestedStory queryById(String id) {
        return this.internalRepo.queryById(id);
    }

    @Override
    public boolean contains(String id) {
        return this.internalRepo.contains(id);
    }
}
