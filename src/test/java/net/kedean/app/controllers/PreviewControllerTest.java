package net.kedean.app.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import net.kedean.news.controllers.PreviewController;
import net.kedean.news.dto.IngestedWithStatus;
import net.kedean.news.dto.IngestionMetadata;
import net.kedean.news.dto.IngestionStatus.Status;
import net.kedean.news.dto.Story;
import net.kedean.news.services.PublicationService;
import net.kedean.news.services.ScreenshotService;

import org.apache.http.client.utils.DateUtils;
import org.joda.time.DateTime;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@WebAppConfiguration
@ContextConfiguration("classpath*:servlet-context.xml")
public class PreviewControllerTest extends AbstractTestNGSpringContextTests {

    private MockMvc             mockMvc;

    @InjectMocks
    private PreviewController   previewController;

    @Mock
    private ScreenshotService   screenshots;

    @Mock
    private PublicationService  publication;

    private static final String PROVIDER          = "provider";
    private static final String IF_MODIFIED_SINCE = "Mon, 14 Dec 2015 21:32:43 GMT";
    private static final String INVALID_ID        = "invalid";
    private static final String PENDING_ID        = "pending";
    private static final String REJECTED_ID       = "rejected";
    private static final String PUBLISHED_ID      = "published";
    private static final String PUBLISHED_LINK    = "http://published";
    private static final byte[] PUBLISHED_DATA    = new byte[] { 'p', 'u', 'b' };
    private static final String CACHED_ID         = "cached";
    private static final byte[] CACHED_DATA       = new byte[] { 'c', 'a', 'c', 'h', 'e', 'd' };
    private static final String CACHED_LINK       = "http://cached";

    private Story               publishedStory, cachedStory, rejectedStory, pendingStory;

    @BeforeMethod
    public void setup() {
        this.previewController = new PreviewController();
        MockitoAnnotations.initMocks(this);
        this.mockMvc = standaloneSetup(this.previewController).build();

        DateTime modifiedCheckDate = new DateTime(DateUtils.parseDate(IF_MODIFIED_SINCE));

        this.publishedStory = new Story();
        this.publishedStory.setLink(PUBLISHED_LINK);
        IngestionMetadata publishedMeta = new IngestionMetadata();
        publishedMeta.setIngestionTime(modifiedCheckDate.minusHours(2).getMillis());
        publishedMeta.setPublishTime(modifiedCheckDate.plusHours(1).getMillis()); // this one has been modified since last cache
        publishedMeta.setExpirationTime(modifiedCheckDate.plusHours(2).getMillis());
        this.publishedStory.setMetadata(publishedMeta);
        Mockito.when(this.publication.queryById(PUBLISHED_ID)).thenReturn(new IngestedWithStatus(PUBLISHED_ID, this.publishedStory, Status.PUBLISHED));
        Mockito.when(this.screenshots.getScreenshot(PUBLISHED_LINK)).thenReturn(PUBLISHED_DATA);

        this.cachedStory = new Story();
        this.cachedStory.setLink(CACHED_LINK);
        IngestionMetadata cachedMeta = new IngestionMetadata();
        cachedMeta.setIngestionTime(modifiedCheckDate.minusHours(2).getMillis());
        cachedMeta.setPublishTime(modifiedCheckDate.minusHours(1).getMillis()); // this one has been modified since last cache
        cachedMeta.setExpirationTime(modifiedCheckDate.plusHours(2).getMillis());
        this.cachedStory.setMetadata(cachedMeta);
        Mockito.when(this.publication.queryById(CACHED_ID)).thenReturn(new IngestedWithStatus(CACHED_ID, this.cachedStory, Status.PUBLISHED));
        Mockito.when(this.screenshots.getScreenshot(CACHED_LINK)).thenReturn(CACHED_DATA);

        this.pendingStory = new Story();
        IngestionMetadata pendingMeta = new IngestionMetadata();
        pendingMeta.setIngestionTime(modifiedCheckDate.minusHours(1).getMillis());
        this.pendingStory.setMetadata(pendingMeta);
        Mockito.when(this.publication.queryById(PENDING_ID)).thenReturn(new IngestedWithStatus(PENDING_ID, this.pendingStory, Status.PENDING));

        this.rejectedStory = new Story();
        IngestionMetadata rejectedMeta = new IngestionMetadata();
        rejectedMeta.setIngestionTime(modifiedCheckDate.minusHours(2).getMillis());
        rejectedMeta.setRejectionTime(modifiedCheckDate.minusHours(1).getMillis());
        this.rejectedStory.setMetadata(rejectedMeta);
        Mockito.when(this.publication.queryById(REJECTED_ID)).thenReturn(new IngestedWithStatus(REJECTED_ID, this.rejectedStory, Status.REJECTED));
    }

    @Test(dataProvider = PROVIDER)
    public void getStoryPreview(String id, Integer status, byte[] expectedData) throws Exception {
        ResultActions result = this.mockMvc.perform(get("/previews/" + id).header("If-Modified-Since", IF_MODIFIED_SINCE)).andExpect(status().is(status));

        if (expectedData != null) {
            result.andExpect(content().contentType(MediaType.IMAGE_PNG)).andExpect(content().bytes(expectedData));
        } else {
            result.andExpect(content().string(""));
        }
    }

    @DataProvider(name = PROVIDER)
    public Object[][] getPreviewData() {
        return new Object[][] { new Object[] { INVALID_ID, 404, null }, new Object[] { PENDING_ID, 404, null }, new Object[] { REJECTED_ID, 404, null },
                new Object[] { PUBLISHED_ID, 200, PUBLISHED_DATA }, new Object[] { CACHED_ID, 304, CACHED_DATA } };
    }
}
