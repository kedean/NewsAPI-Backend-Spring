package net.kedean.app.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

import java.util.Arrays;

import net.kedean.news.controllers.StoryOutputController;
import net.kedean.news.dto.IngestedStory;
import net.kedean.news.dto.IngestedWithStatus;
import net.kedean.news.dto.IngestionMetadata;
import net.kedean.news.dto.IngestionStatus;
import net.kedean.news.dto.IngestionStatus.Status;
import net.kedean.news.dto.Story;
import net.kedean.news.services.PublicationService;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@WebAppConfiguration
@ContextConfiguration("classpath*:servlet-context.xml")
public class StoryOutputControllerTest extends AbstractTestNGSpringContextTests {

    private MockMvc               mockMvc;

    @InjectMocks
    private StoryOutputController outputController;

    @Mock
    private PublicationService    publication;

    private final static String   TEST_LINK    = "testLink", TEST_HEADLINE = "testHeadline";
    private final static String   NOT_REAL_ID  = "fakeId";
    private final static String   PENDING_ID   = "pendingId";
    private final static String   PUBLISHED_ID = "publishedId";
    private final static String   REJECTED_ID  = "rejectedId";

    private IngestedWithStatus    published, pending, rejected;

    @BeforeMethod
    public void setup() {
        this.outputController = new StoryOutputController();
        MockitoAnnotations.initMocks(this);
        this.mockMvc = standaloneSetup(this.outputController).build();

        Story pendingStory = new Story();
        pendingStory.setHeadline(TEST_HEADLINE);
        pendingStory.setLink(TEST_LINK);
        pendingStory.setMetadata(new IngestionMetadata());
        this.pending = new IngestedWithStatus(PENDING_ID, pendingStory, Status.PENDING);

        Mockito.when(this.publication.checkStatus(PENDING_ID)).thenReturn(new IngestionStatus(PENDING_ID, Status.PENDING));
        Mockito.when(this.publication.queryById(PENDING_ID)).thenReturn(this.pending);

        Story publishedStory = new Story();
        publishedStory.setHeadline(TEST_HEADLINE);
        publishedStory.setLink(TEST_LINK);
        publishedStory.setMetadata(new IngestionMetadata());

        this.published = new IngestedWithStatus(PUBLISHED_ID, publishedStory, Status.PUBLISHED);

        Mockito.when(this.publication.queryById(PUBLISHED_ID)).thenReturn(this.published);

        Story rejectedStory = new Story();
        rejectedStory.setHeadline(TEST_HEADLINE);
        rejectedStory.setLink(TEST_LINK);
        rejectedStory.setMetadata(new IngestionMetadata());

        this.rejected = new IngestedWithStatus(REJECTED_ID, rejectedStory, Status.REJECTED);

        Mockito.when(this.publication.queryById(REJECTED_ID)).thenReturn(this.rejected);

        Mockito.when(this.publication.query()).thenReturn(Arrays.asList((IngestedStory) this.published));
    }

    @Test
    public void checkStatus() throws Exception {
        String expectedJSON = "{\"id\":\"" + PENDING_ID + "\",\"status\":\"" + Status.PENDING + "\"}";

        this.mockMvc.perform(get("/stories/" + PENDING_ID + "/status")).andExpect(status().isOk()).andExpect(content().json(expectedJSON));

        Mockito.verify(this.publication).checkStatus(PENDING_ID);
    }

    @Test
    public void checkStatusFake() throws Exception {

        MvcResult result = this.mockMvc.perform(get("/stories/" + NOT_REAL_ID + "/status")).andExpect(status().isNotFound()).andReturn();
        Assert.assertEquals(result.getResponse().getContentLength(), 0);

        Mockito.verify(this.publication).checkStatus(NOT_REAL_ID);
    }

    private static final String SINGLE_STORY_PROVIDER = "singleStoryProvider";

    @Test(dataProvider = SINGLE_STORY_PROVIDER)
    public void getSingleStory(IngestedWithStatus expected, int status) throws Exception {
        String expectedJSON = "{\"id\":\"" + expected.getId() + "\",\"status\":\"" + expected.getStatus() + "\",\"details\":{\"headline\":\"" + expected.getDetails().getHeadline()
                + "\", \"link\":\"" + expected.getDetails().getLink() + "\", \"metadata\":{}}}";
        this.mockMvc.perform(get("/stories/" + expected.getId() + "")).andExpect(status().is(status)).andExpect(content().json(expectedJSON));
    }

    @DataProvider(name = SINGLE_STORY_PROVIDER)
    public Object[][] singleStoryProvider() {
        return new Object[][] { new Object[] { this.published, 200 }, new Object[] { this.pending, 202 }, new Object[] { this.rejected, 404 } };
    }

    @Test
    public void getAllStories() throws Exception {
        String expectedJSON = "[{\"id\":\"" + this.published.getId() + "\",\"details\":{\"headline\":\"" + this.published.getDetails().getHeadline() + "\", \"link\":\""
                + this.published.getDetails().getLink() + "\", \"metadata\":{}}}]";
        this.mockMvc.perform(get("/stories")).andExpect(status().isOk()).andExpect(content().json(expectedJSON));
    }
}
