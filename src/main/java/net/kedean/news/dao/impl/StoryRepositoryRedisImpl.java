package net.kedean.news.dao.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.kedean.news.dao.StoryRepository;
import net.kedean.news.dto.IngestedStory;
import net.kedean.news.dto.IngestionMetadata.DateType;
import net.kedean.news.dto.Story;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Repository;

/**
 * MongoDB implementation of the story repository
 *
 * @author kdean
 *
 */
@Repository
@Scope("prototype")
public class StoryRepositoryRedisImpl implements StoryRepository {

    public class PrefixedFetchCallback implements RedisCallback<Collection<String>> {

        private String prefix;

        public PrefixedFetchCallback(String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Collection<String> doInRedis(RedisConnection connection) throws DataAccessException {

            List<String> binaryKeys = new ArrayList<>();

            Cursor<byte[]> cursor = connection.scan(ScanOptions.scanOptions().match(String.format("*%s.*", this.prefix)).build());
            while (cursor.hasNext()) {
                binaryKeys.add(this.prefix.concat(new String(cursor.next()).split(this.prefix)[1])); // we split at the prefix then prepend it because Jedis adds junk characters
                // before
                // our known prefix
            }

            try {
                cursor.close();
            } catch (IOException e) {
                // do something meaningful
            }

            return binaryKeys;
        }
    }

    private Collection<String> getExistingKeys() {
        return this.redis.execute(new PrefixedFetchCallback(this.keyPrefix));
    }

    @Autowired
    protected RedisTemplate<String, IngestedStory> redis;

    private String                                 keyPrefix;

    public String getKeyPrefix() {
        return this.keyPrefix;
    }

    public void setKeyPrefix(String keyPrefix) {
        this.keyPrefix = keyPrefix;
    }

    @Override
    public void put(String id, Story story) {
        this.put(new IngestedStory(id, story));
    }

    private String makeKey(String id) {
        return String.format("%s.%s", this.keyPrefix, id);
    }

    @Override
    public void put(IngestedStory story) {
        this.redis.opsForValue().set(this.makeKey(story.getId()), story);
    }

    @Override
    public void delete(IngestedStory story) {
        this.redis.delete(this.makeKey(story.getId()));
    }

    @Override
    public Collection<IngestedStory> all() {
        return this.redis.opsForValue().multiGet(this.getExistingKeys());
    }

    @Override
    public Collection<IngestedStory> queryByDatesBefore(long compareTime, DateType type) {
        List<IngestedStory> output = new ArrayList<>();

        for (IngestedStory story : this.all()) { // TODO: this is a very poor implementation and requires getting all values first, is there a way around it?
            switch (type) {
            case EXPIRED:
                if (story.getDetails().getMetadata().getExpirationTime() < compareTime) {
                    output.add(story);
                }
                break;
            case INGESTED:
                if (story.getDetails().getMetadata().getIngestionTime() < compareTime) {
                    output.add(story);
                }
                break;
            case PUBLISHED:
                if (story.getDetails().getMetadata().getPublishTime() < compareTime) {
                    output.add(story);
                }
                break;
            case REJECTED:
                if (story.getDetails().getMetadata().getRejectionTime() < compareTime) {
                    output.add(story);
                }
                break;
            }
        }

        return output;
    }

    @Override
    public Collection<IngestedStory> queryByDatesAfter(long compareTime, DateType type) {
        List<IngestedStory> output = new ArrayList<>();

        for (IngestedStory story : this.all()) {
            switch (type) {
            case EXPIRED:
                if (story.getDetails().getMetadata().getExpirationTime() > compareTime) {
                    output.add(story);
                }
                break;
            case INGESTED:
                if (story.getDetails().getMetadata().getIngestionTime() > compareTime) {
                    output.add(story);
                }
                break;
            case PUBLISHED:
                if (story.getDetails().getMetadata().getPublishTime() > compareTime) {
                    output.add(story);
                }
                break;
            case REJECTED:
                if (story.getDetails().getMetadata().getRejectionTime() > compareTime) {
                    output.add(story);
                }
                break;
            }
        }

        return output;
    }

    @Override
    public IngestedStory queryById(String id) {
        return this.redis.opsForValue().get(this.makeKey(id));
    }

    @Override
    public boolean contains(String id) {
        return this.redis.hasKey(this.makeKey(id));
    }

}
