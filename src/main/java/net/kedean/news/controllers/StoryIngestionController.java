package net.kedean.news.controllers;

import java.net.URI;
import java.net.URISyntaxException;

import net.kedean.news.dto.IngestionStatus;
import net.kedean.news.dto.Story;
import net.kedean.news.services.PublicationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Manages all PUT and POST requests to publish new stories
 *
 * @author kdean
 *
 */
@RestController
public class StoryIngestionController {

    @Autowired
    private PublicationService stories;

    private HttpEntity<IngestionStatus> addStory(Story story) throws URISyntaxException {
        IngestionStatus status = this.stories.publishStory(story);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(new URI(status.getId()));
        return new HttpEntity<IngestionStatus>(status, headers);
    }

    @RequestMapping(value = "/stories", method = { RequestMethod.POST }, headers = "content-type=application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public HttpEntity<IngestionStatus> addJSONStory(@RequestBody Story story) throws URISyntaxException {
        return this.addStory(story);
    }

    @RequestMapping(value = "/stories", method = { RequestMethod.POST }, headers = "content-type=application/x-www-form-urlencoded")
    @ResponseStatus(HttpStatus.CREATED)
    public HttpEntity<IngestionStatus> addFormStory(@ModelAttribute Story story) throws URISyntaxException {
        return this.addStory(story);
    }
}
