package net.kedean.news.dao.impl;

import net.kedean.news.dao.ScreenshotRepository;
import net.kedean.news.dto.Screenshot;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@Scope("prototype")
public class ScreenshotRepositoryRedisImpl implements ScreenshotRepository {

    @Autowired
    protected RedisTemplate<String, Screenshot> redis;

    private static final String                 PREFIX = "screenshot";

    @Override
    public void put(Screenshot screenshot) {
        String key = String.format("%s.%s", PREFIX, screenshot.getId());

        this.redis.opsForValue().set(key, screenshot);
    }

    @Override
    public Screenshot queryById(String id) {
        String key = String.format("%s.%s", PREFIX, id);

        return this.redis.opsForValue().get(key);
    }

}
