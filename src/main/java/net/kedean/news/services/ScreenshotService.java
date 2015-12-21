package net.kedean.news.services;

/**
 * Service for producing and fetching screenshots of a web URL
 *
 * @author kdean
 *
 */
public interface ScreenshotService {

    /**
     * Returns the current output format of generated screenshots. This can be expected to be the format of any result from getScreenshot
     *
     * @return a string representation of image format
     */
    public String getOutputFormat();

    /**
     * Creates a screenshot for the given url, cropped to screenWidth and screenHeight, and resized to scaledWidth and scaledHeight.
     *
     * @param url
     * @param screenWidth
     * @param screenHeight
     * @param scaledWidth
     * @param scaledHeight
     * @return an image in byte array form
     */
    public byte[] getScreenshotFromURL(String url, Integer screenWidth, Integer screenHeight, Integer scaledWidth, Integer scaledHeight);

    /**
     * Creates and stores a screenshot for the given url and associates with the given id. See getScreenshotFromURL for details.
     *
     * @param id
     * @param url
     * @param screenWidth
     * @param screenHeight
     * @param scaledWidth
     * @param scaledHeight
     */
    public void generateScreenshot(String url, Integer screenWidth, Integer screenHeight, Integer scaledWidth, Integer scaledHeight);

    /**
     * Fetches the screenshot identified by id
     *
     * @param url
     * @return an image in byte array form
     */
    public byte[] getScreenshot(String url);

    /**
     * Stores the given image data as a screenshot associated with the given url
     *
     * @param url
     * @param imageData
     */
    public void putScreenshot(String url, byte[] imageData);
}
