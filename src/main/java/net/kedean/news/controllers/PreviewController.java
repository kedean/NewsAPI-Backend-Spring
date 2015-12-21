package net.kedean.news.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.kedean.news.dto.IngestedWithStatus;
import net.kedean.news.services.PublicationService;
import net.kedean.news.services.ScreenshotService;
import net.kedeans.news.exceptions.NotFoundException;

import org.apache.http.client.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Manages all requests for a story preview image
 *
 * @author kdean
 *
 */
@RestController
public class PreviewController {

    @Autowired
    private ScreenshotService   screenshots;

    @Autowired
    private PublicationService  publication;

    private static final String CACHE_CONTROL_PRIVATE = "private";
    private static final String IF_MODIFIED_HEADER    = "If-Modified-Since";

    @RequestMapping(value = "/previews/{id}", method = RequestMethod.GET)
    public HttpEntity<byte[]> getStoryPreview(@PathVariable String id, HttpServletRequest request, HttpServletResponse response) {
        final IngestedWithStatus story = this.publication.queryById(id);

        if (story != null) {
            final byte[] image = this.screenshots.getScreenshot(story.getDetails().getLink());
            if (image != null) {
                // preview doesn't exist yet, if rejected then it was never made, if archived it won't
                // be found by queryById
                final long published = story.getDetails().getMetadata().getPublishTime();
                final long expires = story.getDetails().getMetadata().getExpirationTime();

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.IMAGE_PNG);
                headers.setContentLength(image.length);
                headers.setExpires(expires);
                headers.setCacheControl(CACHE_CONTROL_PRIVATE);
                headers.setLastModified(published);

                String ifModifiedSince = request.getHeader(IF_MODIFIED_HEADER);

                if (ifModifiedSince != null) {
                    Long ifModifiedSinceDate = DateUtils.parseDate(ifModifiedSince).getTime();
                    if (ifModifiedSinceDate > published) {
                        response.setStatus(HttpStatus.NOT_MODIFIED.value());
                    }
                }

                return new HttpEntity<byte[]>(image, headers);
            }
        }

        throw new NotFoundException();

    }
}
