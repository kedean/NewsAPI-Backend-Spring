package net.kedean.app.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import net.kedean.news.controllers.StoryIngestionController;
import net.kedean.news.dto.IngestionStatus;
import net.kedean.news.dto.IngestionStatus.Status;
import net.kedean.news.dto.Story;
import net.kedean.news.services.PublicationService;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@WebAppConfiguration
@ContextConfiguration("classpath*:servlet-context.xml")
public class StoryIngestionControllerTest extends AbstractTestNGSpringContextTests {

    private MockMvc                  mockMvc;

    @InjectMocks
    private StoryIngestionController ingestionController;

    @Mock
    private PublicationService       publication;

    private final static String      TEST_LINK = "testLink", TEST_HEADLINE = "testHeadline", TEST_ID = "testId";

    @BeforeMethod
    public void setup() {
        this.ingestionController = new StoryIngestionController();
        MockitoAnnotations.initMocks(this);
        this.mockMvc = standaloneSetup(this.ingestionController).build();

        Mockito.when(this.publication.publishStory(Mockito.any(Story.class))).thenReturn(new IngestionStatus(TEST_ID, Status.PENDING));
    }

    private void verifyPublication() {
        ArgumentCaptor<Story> storyCaptor = ArgumentCaptor.forClass(Story.class);
        Mockito.verify(this.publication).publishStory(storyCaptor.capture());

        Story story = storyCaptor.getValue();
        Assert.assertEquals(story.getHeadline(), TEST_HEADLINE);
        Assert.assertEquals(story.getLink(), TEST_LINK);
    }

    @Test
    public void jsonSubmission() throws Exception {
        String jsonData = "{\"headline\":\"" + TEST_HEADLINE + "\",\"link\":\"" + TEST_LINK + "\"}";
        this.mockMvc.perform(post("/stories").content(jsonData).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated()).andExpect(redirectedUrl(TEST_ID));

        this.verifyPublication();
    }

    @Test
    public void formSubmission() throws Exception {
        this.mockMvc.perform(post("/stories").param("headline", TEST_HEADLINE).param("link", TEST_LINK).contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isCreated()).andExpect(redirectedUrl(TEST_ID));

        this.verifyPublication();
    }
}
