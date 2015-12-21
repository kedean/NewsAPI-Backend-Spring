package net.kedean.news.services.impl;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.annotation.Resource;
import javax.imageio.ImageIO;

import net.anthavio.phanbedder.Phanbedder;
import net.kedean.news.dao.ScreenshotRepository;
import net.kedean.news.dto.Screenshot;
import net.kedean.news.services.ScreenshotService;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ScreenshotServicePhantomImpl implements ScreenshotService {

    public static final String   OUTPUT_FORMAT = "png";

    private static final Logger  LOGGER        = LoggerFactory.getLogger(ScreenshotServicePhantomImpl.class);

    @Resource(name = "screenshots")
    private ScreenshotRepository repository;

    public BufferedImage takeScreenshot(String url, Integer screenWidth, Integer screenHeight) {
        final File phantomjs = Phanbedder.unpack();
        final DesiredCapabilities dcaps = new DesiredCapabilities();
        dcaps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY, phantomjs.getAbsolutePath());

        dcaps.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "resourceTimeout", 10000);
        dcaps.setCapability(PhantomJSDriverService.PHANTOMJS_PAGE_SETTINGS_PREFIX + "loadImages", true);

        dcaps.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, new String[] { "--web-security=no", "--ignore-ssl-errors=yes" });

        PhantomJSDriver driver = new PhantomJSDriver(dcaps);
        driver.manage().window().setSize(new Dimension(screenWidth, screenHeight));
        driver.get(url);
        File output = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        driver.close();
        try {
            return ImageIO.read(output);
        } catch (IOException e) {
            LOGGER.error("Could not parse screenshot", e);
        }
        return null;
    }

    public BufferedImage cropAndScaleScreenshot(BufferedImage screenshot, Integer screenWidth, Integer screenHeight, Integer scaledWidth, Integer scaledHeight) {
        Image destImage = screenshot.getSubimage(0, 0, screenWidth, screenHeight).getScaledInstance(scaledWidth, scaledHeight, BufferedImage.SCALE_REPLICATE);
        BufferedImage renderedScale = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_RGB);
        Graphics g = renderedScale.getGraphics();
        g.drawImage(destImage, 0, 0, null);
        g.dispose();

        return renderedScale;
    }

    public byte[] processToBytes(BufferedImage screenshot) {
        ByteArrayOutputStream screenshotStream = new ByteArrayOutputStream();
        try {
            ImageIO.write(screenshot, OUTPUT_FORMAT, screenshotStream);
        } catch (IOException e) {
            screenshotStream = null;
            LOGGER.error("Could not encode image as " + OUTPUT_FORMAT, e);
        }
        return screenshotStream.toByteArray();
    }

    @Override
    public byte[] getScreenshotFromURL(String url, Integer screenWidth, Integer screenHeight, Integer scaledWidth, Integer scaledHeight) {
        BufferedImage raw = this.takeScreenshot(url, screenWidth, screenHeight);
        BufferedImage croppedAndScaled = this.cropAndScaleScreenshot(raw, screenWidth, screenHeight, scaledWidth, scaledHeight);
        return this.processToBytes(croppedAndScaled);
    }

    @Override
    public String getOutputFormat() {
        return OUTPUT_FORMAT;
    }

    @Override
    public void generateScreenshot(String url, Integer screenWidth, Integer screenHeight, Integer scaledWidth, Integer scaledHeight) {
        final byte[] imageData = this.getScreenshotFromURL(url, screenWidth, screenHeight, scaledWidth, scaledHeight);
        this.putScreenshot(url, imageData);
    }

    @Override
    public byte[] getScreenshot(String url) {
        Screenshot output = this.repository.queryById(url);

        if (output == null) {
            return null;
        } else {
            return output.getData();
        }
    }

    @Override
    public void putScreenshot(String url, byte[] imageData) {
        this.repository.put(new Screenshot(url, imageData));
    }

}
