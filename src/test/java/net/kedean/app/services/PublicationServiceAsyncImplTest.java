package net.kedean.app.services;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import net.kedean.news.dao.impl.ArchivedStoryRepositoryImpl;
import net.kedean.news.dao.impl.PendingStoryRepositoryImpl;
import net.kedean.news.dao.impl.PublishedStoryRepositoryImpl;
import net.kedean.news.dao.impl.RejectedStoryRepositoryImpl;
import net.kedean.news.dto.IngestedStory;
import net.kedean.news.dto.IngestionMetadata;
import net.kedean.news.dto.IngestionMetadata.DateType;
import net.kedean.news.dto.IngestionStatus;
import net.kedean.news.dto.IngestionStatus.Status;
import net.kedean.news.dto.RejectedStory;
import net.kedean.news.dto.Story;
import net.kedean.news.services.ScreenshotService;
import net.kedean.news.services.impl.PublicationServiceAsyncImpl;

import org.joda.time.DateTime;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PublicationServiceAsyncImplTest {

    @InjectMocks
    private PublicationServiceAsyncImpl  publicationService;

    @Mock
    private PublishedStoryRepositoryImpl published;

    @Mock
    private PendingStoryRepositoryImpl   pending;

    @Mock
    private RejectedStoryRepositoryImpl  rejected;

    @Mock
    private ArchivedStoryRepositoryImpl  archived;

    @Mock
    private ScreenshotService            screenshots;

    @BeforeMethod
    public void setup() {
        this.publicationService = new PublicationServiceAsyncImpl();
        MockitoAnnotations.initMocks(this);
    }

    /*
     * Common public methods
     */
    @Test
    public void publishStory() throws InterruptedException {
        long start = DateTime.now().getMillis();

        Thread.sleep(5); // just to guarantee the modified time gets updated to a later time

        Story story = new Story();
        story.setHeadline("test");
        story.setLink("test");

        this.publicationService.publishStory(story);

        Mockito.verify(this.pending).put(Mockito.anyString(), Mockito.eq(story));

        Assert.assertTrue(this.publicationService.getLastModified() > start, "Methods used did not update last modified timestamp");
    }

    @Test
    public void query() throws InterruptedException {
        long start = DateTime.now().getMillis();

        Thread.sleep(5);

        List<IngestedStory> expected = Arrays.asList(Mockito.mock(IngestedStory.class), Mockito.mock(IngestedStory.class));
        Mockito.when(this.published.all()).thenReturn(expected);

        Collection<IngestedStory> result = this.publicationService.query();

        Assert.assertEquals(result.size(), expected.size());

        for (int i = 0; i < result.size(); i++) {
            Assert.assertTrue(result.contains(expected.get(i)));
        }

        Assert.assertFalse(this.publicationService.getLastModified() > start, "An unexpected change to the last modified timestamp was made");
    }

    @Test(dependsOnMethods = "publishStory")
    public void queryById() throws InterruptedException {
        long start = DateTime.now().getMillis();

        Thread.sleep(5);

        String testId = "testId";
        Story story = new Story();
        story.setHeadline("testHeadline");
        story.setLink("testLink");
        IngestedStory expected = new IngestedStory(testId, story);

        Mockito.when(this.published.queryById(testId)).thenReturn(expected);

        IngestedStory result = this.publicationService.queryById(testId);
        Assert.assertSame(expected.getDetails(), result.getDetails());

        Assert.assertFalse(this.publicationService.getLastModified() > start, "An unexpected change to the last modified timestamp was made");
    }

    /*
     * Status check methods Note, these can't be done with a data provider because it breaks the mocking relationship
     */

    @Test
    public void checkStatusPublished() throws InterruptedException {
        long start = DateTime.now().getMillis();

        Thread.sleep(5);

        String testId = UUID.randomUUID().toString();

        Mockito.when(this.published.contains(testId)).thenReturn(true);
        IngestionStatus result = this.publicationService.checkStatus(testId);

        Assert.assertEquals(result.getStatus(), Status.PUBLISHED);
        Assert.assertFalse(this.publicationService.getLastModified() > start, "An unexpected change to the last modified timestamp was made");
    }

    @Test
    public void checkStatusPending() throws InterruptedException {
        long start = DateTime.now().getMillis();
        Thread.sleep(5);
        String testId = UUID.randomUUID().toString();

        Mockito.when(this.pending.contains(testId)).thenReturn(true);
        IngestionStatus result = this.publicationService.checkStatus(testId);

        Assert.assertEquals(result.getStatus(), Status.PENDING);
        Assert.assertFalse(this.publicationService.getLastModified() > start, "An unexpected change to the last modified timestamp was made");
    }

    @Test
    public void checkStatusRejected() throws InterruptedException {
        long start = DateTime.now().getMillis();
        Thread.sleep(5);
        String testId = UUID.randomUUID().toString();

        Mockito.when(this.rejected.contains(testId)).thenReturn(true);
        IngestionStatus result = this.publicationService.checkStatus(testId);

        Assert.assertEquals(result.getStatus(), Status.REJECTED);
        Assert.assertFalse(this.publicationService.getLastModified() > start, "An unexpected change to the last modified timestamp was made");
    }

    @Test
    public void checkStatusNotFound() throws InterruptedException {
        long start = DateTime.now().getMillis();
        Thread.sleep(5);
        String testId = UUID.randomUUID().toString();

        IngestionStatus result = this.publicationService.checkStatus(testId);

        Assert.assertEquals(result.getStatus(), Status.NOT_FOUND);
        Assert.assertFalse(this.publicationService.getLastModified() > start, "An unexpected change to the last modified timestamp was made");
    }

    /*
     * Methods unique to this implementation
     */

    @Test
    public void processPending() throws InterruptedException {
        long start = DateTime.now().getMillis();
        Thread.sleep(5);

        // prep with stories
        String invalidIdNoLink = UUID.randomUUID().toString();
        Story invalidStoryNoLink = new Story();
        invalidStoryNoLink.setHeadline("Test");
        IngestionMetadata invalidMetadataNoLink = new IngestionMetadata();
        invalidMetadataNoLink.setIngestionTime(DateTime.now().getMillis());
        invalidStoryNoLink.setMetadata(invalidMetadataNoLink);

        String invalidIdNoHeadline = UUID.randomUUID().toString();
        Story invalidStoryNoHeadline = new Story();
        invalidStoryNoHeadline.setLink("http://localhost");
        IngestionMetadata invalidMetadataNoHeadline = new IngestionMetadata();
        invalidMetadataNoHeadline.setIngestionTime(DateTime.now().getMillis());
        invalidStoryNoHeadline.setMetadata(invalidMetadataNoHeadline);

        String validId = UUID.randomUUID().toString();
        Story validStory = new Story();
        validStory.setHeadline("Test");
        validStory.setLink("http://localhost");
        IngestionMetadata validMetadata = new IngestionMetadata();
        validMetadata.setIngestionTime(DateTime.now().getMillis());
        validStory.setMetadata(validMetadata);

        List<IngestedStory> pendingStories = Arrays.asList(new IngestedStory(validId, validStory), new IngestedStory(invalidIdNoHeadline, invalidStoryNoHeadline),
                new IngestedStory(invalidIdNoLink, invalidStoryNoLink));
        Mockito.when(this.pending.all()).thenReturn(pendingStories);

        // process them
        this.publicationService.processPending();

        // verify output
        Mockito.verify(this.screenshots, Mockito.times(1)).generateScreenshot(Mockito.eq(validStory.getLink()), Mockito.anyInt(), Mockito.anyInt(), Mockito.anyInt(),
                Mockito.anyInt());

        Mockito.verify(this.pending, Mockito.times(3)).delete(Mockito.any(IngestedStory.class)); // all 3 stories that were pending should have been processed fully

        // guarantee that each of the invalid stories were rejected for the right reasons
        ArgumentCaptor<String> rejectedIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Story> rejectedStoryCaptor = ArgumentCaptor.forClass(Story.class);
        Mockito.verify(this.rejected, Mockito.times(2)).put(rejectedIdCaptor.capture(), rejectedStoryCaptor.capture());

        List<String> rejectedIds = rejectedIdCaptor.getAllValues();
        List<Story> rejectedStories = rejectedStoryCaptor.getAllValues();

        Assert.assertEquals(rejectedIds.size(), rejectedStories.size()); // just to prevent NPE
        Assert.assertEquals(rejectedIds.size(), 2);

        for (int i = 0; i < rejectedIds.size(); i++) {
            String id = rejectedIds.get(i);
            RejectedStory story = (RejectedStory) rejectedStories.get(i);

            if (id.equals(invalidIdNoHeadline)) { // verify against the no-headline story
                Assert.assertEquals(story.getNote(), "Missing headline");
                Assert.assertEquals(story.getMetadata(), invalidStoryNoHeadline.getMetadata());
            } else if (id.equals(invalidIdNoLink)) { // verify against the no-link story
                Assert.assertEquals(story.getNote(), "Missing link");
                Assert.assertEquals(story.getMetadata(), invalidStoryNoLink.getMetadata());
            } else {
                Assert.fail("Unexpected story id: " + id);
            }
            Assert.assertNull(story.getHeadline());
            Assert.assertNull(story.getLink());
        }

        Assert.assertTrue(this.publicationService.getLastModified() > start, "Methods used did not update last modified timestamp");
    }

    @Test
    public void cullExpired() throws InterruptedException {
        long start = DateTime.now().getMillis();

        Thread.sleep(5);

        // prep stories
        String id1 = UUID.randomUUID().toString();
        Story story1 = new Story();
        story1.setHeadline("Test");
        story1.setLink("http://localhost");
        IngestionMetadata metadata1 = new IngestionMetadata();
        metadata1.setIngestionTime(DateTime.now().getMillis());
        metadata1.setPublishTime(DateTime.now().getMillis());
        metadata1.setExpirationTime(DateTime.now().getMillis());
        story1.setMetadata(metadata1);

        String id2 = UUID.randomUUID().toString();
        Story story2 = new Story();
        story2.setHeadline("Test");
        story2.setLink("http://localhost");
        IngestionMetadata metadata2 = new IngestionMetadata();
        metadata2.setIngestionTime(DateTime.now().getMillis());
        metadata2.setPublishTime(DateTime.now().getMillis());
        metadata2.setExpirationTime(DateTime.now().getMillis());
        story2.setMetadata(metadata2);

        List<IngestedStory> expiredStories = Arrays.asList(new IngestedStory(id1, story1), new IngestedStory(id2, story2));
        Mockito.when(this.published.queryByDatesBefore(Mockito.anyLong(), Mockito.eq(DateType.EXPIRED))).thenReturn(expiredStories);

        // cull the expired ones
        this.publicationService.cullExpired();

        // check the interactions
        Mockito.verify(this.published, Mockito.times(2)).delete(Mockito.any(IngestedStory.class));
        Mockito.verify(this.archived, Mockito.times(2)).put(Mockito.any(IngestedStory.class));
        Mockito.verifyZeroInteractions(this.rejected);
        Mockito.verifyZeroInteractions(this.pending);
        Assert.assertTrue(this.publicationService.getLastModified() > start, "Methods used did not update last modified timestamp");
    }

    @Test
    public void getLastModified() {
        Assert.assertNotNull(this.publicationService.getLastModified());
    }
}
