package net.kedean.news.services.impl;

import java.util.Collection;
import java.util.UUID;

import javax.annotation.Resource;

import net.kedean.news.dao.impl.ArchivedStoryRepositoryImpl;
import net.kedean.news.dao.impl.PendingStoryRepositoryImpl;
import net.kedean.news.dao.impl.PublishedStoryRepositoryImpl;
import net.kedean.news.dao.impl.RejectedStoryRepositoryImpl;
import net.kedean.news.dto.IngestedStory;
import net.kedean.news.dto.IngestedWithStatus;
import net.kedean.news.dto.IngestionMetadata.DateType;
import net.kedean.news.dto.IngestionStatus;
import net.kedean.news.dto.RejectedStory;
import net.kedean.news.dto.Story;
import net.kedean.news.services.PublicationService;
import net.kedean.news.services.ScreenshotService;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PublicationServiceAsyncImpl implements PublicationService {

    private static final Logger          LOGGER       = LoggerFactory.getLogger(PublicationServiceAsyncImpl.class);

    @Resource(name = "publishedStories")
    private PublishedStoryRepositoryImpl published;

    @Resource(name = "pendingStories")
    private PendingStoryRepositoryImpl   pending;

    @Resource(name = "rejectedStories")
    private RejectedStoryRepositoryImpl  rejected;

    @Resource(name = "archivedStories")
    private ArchivedStoryRepositoryImpl  archived;

    @Autowired
    private ScreenshotService            screenshots;

    private Long                         lastModified = DateTime.now().getMillis();

    @Override
    public IngestionStatus publishStory(Story story) {
        String id = UUID.randomUUID().toString();
        this.pending.put(id, story);
        this.markModified();
        return IngestionStatus.Pending(id);
    }

    @Override
    public Collection<IngestedStory> query() {
        return this.published.all();
    }

    @Override
    public IngestedWithStatus queryById(String id) {
        IngestedStory output = this.published.queryById(id);

        if (output == null) {
            output = this.pending.queryById(id);

            if (output == null) {
                output = this.rejected.queryById(id);

                if (output == null) {
                    return null;
                } else {
                    return IngestedWithStatus.Rejected(output);
                }
            } else {
                return IngestedWithStatus.Pending(output);
            }
        } else {
            return IngestedWithStatus.Published(output);
        }
    }

    @Override
    public IngestionStatus checkStatus(String id) {
        if (this.published.contains(id)) {
            return IngestionStatus.Published(id);
        } else if (this.pending.contains(id)) {
            return IngestionStatus.Pending(id);
        } else if (this.rejected.contains(id)) {
            return IngestionStatus.Rejected(id);
        } else {
            return IngestionStatus.NotFound(id);
        }
    }

    private void rejectStory(IngestedStory story, String reason) {
        RejectedStory rejectedStory = new RejectedStory();
        rejectedStory.setNote(reason);
        rejectedStory.setMetadata(story.getDetails().getMetadata());
        this.rejected.put(story.getId(), rejectedStory);
    }

    private void normalizeLink(IngestedStory story) {
        String link = story.getDetails().getLink();

        if (!link.startsWith("http://") && !link.startsWith("https://")) { // does it have a protocol? If not, prepend one. TODO: modify to allow for non-http protocols
            link = "http://".concat(link);
            story.getDetails().setLink(link);
        }
    }

    @Transactional
    public void processStory(IngestedStory story) {
        if (StringUtils.isEmpty(story.getDetails().getHeadline())) {
            this.rejectStory(story, "Missing headline");
        } else if (StringUtils.isEmpty(story.getDetails().getLink())) {
            this.rejectStory(story, "Missing link");
        } else {
            this.normalizeLink(story);
            this.screenshots.generateScreenshot(story.getDetails().getLink(), 1366, 768, 300, 169);
            this.published.put(story.getId(), story.getDetails());
        }

        this.pending.delete(story); // wait to delete from pending until it has been added to one of the other repos first
        this.markModified();
    }

    @Scheduled(fixedDelay = 500)
    public void processPending() {
        Collection<IngestedStory> pendingStories = this.pending.all();

        if (pendingStories.size() > 0) {
            LOGGER.info("Processing pending stories");

            for (IngestedStory story : pendingStories) {
                try {
                    this.processStory(story);
                } catch (Exception e) {
                    LOGGER.error("Could not process story with id " + story.getId() + ", rejecting instead", e);
                    try {
                        this.rejectStory(story, "Error while processing");
                        this.pending.delete(story);
                        this.markModified();
                    } catch (Exception e2) {
                        LOGGER.error("Fatal error processing story with id " + story.getId(), e2);
                    }
                }
            }
        }
    }

    @Scheduled(fixedDelay = 10000)
    public void cullExpired() {
        Collection<IngestedStory> expiredStories = this.published.queryByDatesBefore(DateTime.now().getMillis(), DateType.EXPIRED);

        if (expiredStories.size() > 0) {
            LOGGER.info("Culling expired stories");

            for (IngestedStory story : expiredStories) {
                this.published.delete(story);
                this.archived.put(story);
                this.markModified();
            }
        }
    }

    private void markModified() {
        this.lastModified = DateTime.now().getMillis();
    }

    @Override
    public Long getLastModified() {
        return this.lastModified;
    }
}
