package net.kedean.news.dao.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.kedean.news.dao.StoryRepository;
import net.kedean.news.dto.IngestedStory;
import net.kedean.news.dto.IngestionMetadata.DateType;
import net.kedean.news.dto.Story;

public class StoryRepositoryInMemImpl implements StoryRepository {

    private Map<String, IngestedStory> stories = new HashMap<>();

    @Override
    public void put(String id, Story story) {
        this.put(new IngestedStory(id, story));
    }

    @Override
    public void put(IngestedStory story) {
        this.stories.put(story.getId(), story);
    }

    @Override
    public void delete(IngestedStory story) {
        this.stories.remove(story.getId());
    }

    @Override
    public Collection<IngestedStory> all() {
        return this.stories.values();
    }

    @Override
    public Collection<IngestedStory> queryByDatesBefore(long compareTime, DateType type) {
        List<IngestedStory> output = new ArrayList<>();

        for (IngestedStory story : this.stories.values()) {
            switch (type) {
            case EXPIRED:
                if (story.getDetails().getMetadata().getExpirationTime() < compareTime) {
                    output.add(story);
                }
                break;
            case INGESTED:
                if (story.getDetails().getMetadata().getIngestionTime() < compareTime) {
                    output.add(story);
                }
                break;
            case PUBLISHED:
                if (story.getDetails().getMetadata().getPublishTime() < compareTime) {
                    output.add(story);
                }
                break;
            case REJECTED:
                if (story.getDetails().getMetadata().getRejectionTime() < compareTime) {
                    output.add(story);
                }
                break;
            }
        }

        return output;
    }

    @Override
    public Collection<IngestedStory> queryByDatesAfter(long compareTime, DateType type) {
        List<IngestedStory> output = new ArrayList<>();

        for (IngestedStory story : this.stories.values()) {
            switch (type) {
            case EXPIRED:
                if (story.getDetails().getMetadata().getExpirationTime() > compareTime) {
                    output.add(story);
                }
                break;
            case INGESTED:
                if (story.getDetails().getMetadata().getIngestionTime() > compareTime) {
                    output.add(story);
                }
                break;
            case PUBLISHED:
                if (story.getDetails().getMetadata().getPublishTime() > compareTime) {
                    output.add(story);
                }
                break;
            case REJECTED:
                if (story.getDetails().getMetadata().getRejectionTime() > compareTime) {
                    output.add(story);
                }
                break;
            }
        }

        return output;
    }

    @Override
    public IngestedStory queryById(String id) {
        return this.stories.get(id);
    }

    @Override
    public boolean contains(String id) {
        return this.stories.containsKey(id);
    }

}
