package net.kedean.app.services;

import java.awt.image.BufferedImage;
import java.util.UUID;

import net.kedean.news.dao.ScreenshotRepository;
import net.kedean.news.dto.Screenshot;
import net.kedean.news.services.impl.ScreenshotServicePhantomImpl;

import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ScreenshotServicePhantomImplTest {

    @InjectMocks
    @Spy
    private ScreenshotServicePhantomImpl screenshotService;

    @Mock
    private ScreenshotRepository         repository;

    private static final String          SCREENSHOT_DATA = "screenshotData";

    @BeforeMethod
    public void setup() {
        this.screenshotService = Mockito.spy(new ScreenshotServicePhantomImpl());
        this.repository = Mockito.mock(ScreenshotRepository.class);
        MockitoAnnotations.initMocks(this);
    }

    @Test(dataProvider = SCREENSHOT_DATA)
    public void takeScreenshot(String url, int screenWidth, int screenHeight, int scaledWidth, int scaledHeight) {
        BufferedImage result = this.screenshotService.takeScreenshot(url, screenWidth, screenHeight);
        Assert.assertEquals(result.getWidth(), screenWidth);
        Assert.assertEquals(result.getHeight(), screenHeight);
    }

    @Test(dataProvider = SCREENSHOT_DATA)
    public void cropAndScaleScreenshot(String url, int screenWidth, int screenHeight, int scaledWidth, int scaledHeight) {
        BufferedImage input = Mockito.spy(new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB));
        BufferedImage result = this.screenshotService.cropAndScaleScreenshot(input, screenWidth, screenHeight, scaledWidth, scaledHeight);
        Assert.assertEquals(result.getWidth(), scaledWidth);
        Assert.assertEquals(result.getHeight(), scaledHeight);
        Mockito.verify(input).getSubimage(0, 0, screenWidth, screenHeight);
    }

    @Test
    public void processToBytes() {
        BufferedImage input = new BufferedImage(1000, 1000, BufferedImage.TYPE_INT_RGB);
        byte[] result = this.screenshotService.processToBytes(input);
        Assert.assertTrue(result.length > 0, "Result must be a non-empty byte array");
    }

    @Test(dataProvider = SCREENSHOT_DATA, dependsOnMethods = { "takeScreenshot", "cropAndScaleScreenshot", "processToBytes" })
    public void getScreenshotFromURL(String url, int screenWidth, int screenHeight, int scaledWidth, int scaledHeight) {
        byte[] result = this.screenshotService.getScreenshotFromURL(url, screenWidth, screenHeight, scaledWidth, scaledHeight);
        Assert.assertTrue(result.length > 0, "Result must be a non-empty array");
        Mockito.verify(this.screenshotService, Mockito.times(1)).takeScreenshot(url, screenWidth, screenHeight);
        Mockito.verify(this.screenshotService).cropAndScaleScreenshot(Mockito.any(BufferedImage.class), Mockito.eq(screenWidth), Mockito.eq(screenHeight), Mockito.eq(scaledWidth),
                Mockito.eq(scaledHeight));
        Mockito.verify(this.screenshotService).processToBytes(Mockito.any(BufferedImage.class));
    }

    @Test(dataProvider = SCREENSHOT_DATA, dependsOnMethods = { "getScreenshotFromURL", "putScreenshot" })
    public void generateScreenshot(String url, int screenWidth, int screenHeight, int scaledWidth, int scaledHeight) {

        this.screenshotService.generateScreenshot(url, screenWidth, screenHeight, scaledWidth, scaledHeight);

        Mockito.verify(this.screenshotService).getScreenshotFromURL(url, screenWidth, screenHeight, scaledWidth, scaledHeight);
        Mockito.verify(this.screenshotService).putScreenshot(Mockito.eq(url), Mockito.any(byte[].class));
    }

    @Test
    public void putScreenshot() {
        String testId = "test";
        byte[] testImage = new byte[] { 'A', 'B', 'C' };

        ArgumentCaptor<Screenshot> screenshotCaptor = ArgumentCaptor.forClass(Screenshot.class);

        this.screenshotService.putScreenshot(testId, testImage);

        Mockito.verify(this.repository).put(screenshotCaptor.capture());
        Screenshot result = screenshotCaptor.getValue();
        Assert.assertEquals(result.getId(), testId);
        Assert.assertEquals(result.getData(), testImage);
    }

    @Test
    public void getScreenshot() {
        String id = UUID.randomUUID().toString();
        Screenshot screenshot = Mockito.mock(Screenshot.class);
        Mockito.when(this.repository.queryById(id)).thenReturn(screenshot);
        this.screenshotService.getScreenshot(id);
        Mockito.verify(screenshot, Mockito.times(1)).getData();
    }

    @Test
    public void getScreenshotNotFound() {
        String id = UUID.randomUUID().toString();
        byte[] result = this.screenshotService.getScreenshot(id);
        Assert.assertNull(result);
    }

    @Test
    public void getOutputFormat() {
        Assert.assertNotNull(this.screenshotService.getOutputFormat());
    }

    @DataProvider(name = SCREENSHOT_DATA)
    public Object[][] screenshotData() {
        return new Object[][] { new Object[] { "//", 400, 300, 200, 100 } // using an invalid address guarantes the screenshot is the smallest size allowable
        };
    }
}
