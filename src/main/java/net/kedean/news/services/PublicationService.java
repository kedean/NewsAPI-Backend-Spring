package net.kedean.news.services;

import java.util.Collection;

import net.kedean.news.dto.IngestedStory;
import net.kedean.news.dto.IngestedWithStatus;
import net.kedean.news.dto.IngestionStatus;
import net.kedean.news.dto.Story;

/**
 * Service for interacting with the underlying story storage mechanisms. Takes care of all publishing operations.
 *
 * @author kdean
 *
 */
public interface PublicationService {

    /**
     * Queue up a single story for publication
     *
     * @param story
     * @return An IngestionStatus object indicating the initial status of the story (may be pending or published)
     */
    public IngestionStatus publishStory(Story story);

    /**
     * Fetches all published stories (excluding pending, rejected, and archived)
     *
     * @return a list of stories
     */
    public Collection<IngestedStory> query();

    /**
     * Fetches a single story by identifier, regardless of its status. Returned value will have a status attached.
     *
     * @param id
     * @return an ingested story
     */
    public IngestedWithStatus queryById(String id);

    /**
     * Fetches the current status of a single story, particularly useful in asynchronous implementations.
     *
     * @param id
     * @return The status of a story, plus its id.
     */
    public IngestionStatus checkStatus(String id);

    /**
     * Determines when the story repositories were most recently changed
     *
     * @return a unix timestamp
     */
    public Long getLastModified();
}
