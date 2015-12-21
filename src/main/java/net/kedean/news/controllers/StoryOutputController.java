package net.kedean.news.controllers;

import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.kedean.news.dto.IngestedStory;
import net.kedean.news.dto.IngestedWithStatus;
import net.kedean.news.dto.IngestionStatus;
import net.kedean.news.dto.IngestionStatus.Status;
import net.kedean.news.services.PublicationService;
import net.kedeans.news.exceptions.NotFoundException;

import org.apache.http.client.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Manages all requests for existing stories (regardless of status)
 *
 * @author kdean
 *
 */
@RestController
public class StoryOutputController {

    @Autowired
    private PublicationService  stories;

    private static final String CACHE_CONTROL_PRIVATE = "private";
    private static final String IF_MODIFIED_HEADER    = "If-Modified-Since";

    @RequestMapping(value = "/stories", method = RequestMethod.GET, headers = "accept=application/json")
    public HttpEntity<Collection<IngestedStory>> allStories(HttpServletRequest request, HttpServletResponse response) {
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(CACHE_CONTROL_PRIVATE);

        Long lastModified = this.stories.getLastModified();
        if (lastModified != null) {
            headers.setLastModified(lastModified);
        }

        String ifModifiedSince = request.getHeader(IF_MODIFIED_HEADER);

        if (ifModifiedSince != null) {
            Long ifModifiedSinceDate = DateUtils.parseDate(ifModifiedSince).getTime();
            if (ifModifiedSinceDate <= lastModified) {
                response.setStatus(HttpStatus.NOT_MODIFIED.value());
            }
        }

        return new HttpEntity<>(this.stories.query(), headers);
    }

    @RequestMapping(value = "/stories/{id}/status", method = RequestMethod.GET, headers = "accept=application/json")
    public IngestionStatus checkStatus(@PathVariable String id) {
        IngestionStatus result = this.stories.checkStatus(id);

        if (result != null) {
            return result;
        } else {
            throw new NotFoundException();
        }
    }

    @RequestMapping(value = "/stories/{id}", method = RequestMethod.GET, headers = "accept=application/json")
    public IngestedWithStatus singleStory(@PathVariable String id, HttpServletResponse response) {
        IngestedWithStatus story = this.stories.queryById(id);

        if (story == null || story.getStatus().equals(Status.REJECTED)) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
        } else if (story.getStatus().equals(Status.PENDING)) {
            response.setStatus(HttpStatus.ACCEPTED.value());
        }

        return story;
    }
}
