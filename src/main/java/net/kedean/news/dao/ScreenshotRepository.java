package net.kedean.news.dao;

import net.kedean.news.dto.Screenshot;

/**
 * Storage mechanism for story screenshots
 * 
 * @author kdean
 *
 */
public interface ScreenshotRepository {

    public void put(Screenshot screenshot);

    public Screenshot queryById(String id);
}
