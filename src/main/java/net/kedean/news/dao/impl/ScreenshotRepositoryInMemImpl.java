package net.kedean.news.dao.impl;

import java.util.HashMap;
import java.util.Map;

import net.kedean.news.dao.ScreenshotRepository;
import net.kedean.news.dto.Screenshot;

import org.springframework.stereotype.Repository;

/**
 * Implementation of the screenshot repository that stores each screenshot in an in-memory hash map. Data is purged on restart of the application.
 *
 * @author kdean
 *
 */
@Repository
public class ScreenshotRepositoryInMemImpl implements ScreenshotRepository {

    Map<String, Screenshot> screenshots = new HashMap<>();

    @Override
    public void put(Screenshot screenshot) {
        this.screenshots.put(screenshot.getId(), screenshot);
    }

    @Override
    public Screenshot queryById(String id) {
        return this.screenshots.get(id);
    }
}
