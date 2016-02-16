package com.cicdaas.mocknasasoundapiservice.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cicdaas.mocknasasoundapiservice.cache.SoundCache;
import com.cicdaas.mocknasasoundapiservice.dto.NASAGETSoundTrackResponse;
import com.cicdaas.mocknasasoundapiservice.dto.SoundTrack;

import ch.qos.logback.classic.Logger;

@Controller
public class MockNASASoundAPIServiceController {

    private static final Logger LOG = (Logger) LoggerFactory.getLogger(MockNASASoundAPIServiceController.class);

    @RequestMapping(value = "/healthcheck", method = RequestMethod.GET, produces="text/plain")
    @ResponseBody
    public String isMockServiceAlive() {
        return "alive";
    }

    @RequestMapping(value = "/planetary/sounds", method = RequestMethod.GET, produces="application/json")
    @ResponseBody
    public ResponseEntity<NASAGETSoundTrackResponse> getSounds(HttpServletRequest request) {
        String apiKey = request.getParameter("api_key");
        String query = request.getParameter("q");
        String limitStr = request.getParameter("limit");

        // validate
        if (StringUtils.isEmpty(apiKey)) {
            // raise error
            LOG.error("Request w/o API KEY! Returning FORBIDDEN");
            return new ResponseEntity<NASAGETSoundTrackResponse>(HttpStatus.FORBIDDEN);
        }

        NASAGETSoundTrackResponse response = new NASAGETSoundTrackResponse();
        int limit = getLimit(limitStr);
        List<SoundTrack> sounds = null;
        if (!StringUtils.isEmpty(query)) {
            // fetch user specified no of records
            sounds = SoundCache.getInstance().filter(query, limit);
            if (sounds.isEmpty()) {
                // when there is no match sound track, return the default!
                sounds = SoundCache.getInstance().getAll(limit);
            }
        } else {
            // fetch default no of records
            query = "";
            sounds = SoundCache.getInstance().getAll(limit);
        }
        LOG.debug("Processing completed! Reqest from API KEY: " + apiKey + " , Query: " + query + " , Limit: " + limit);

        response.setCount(sounds.size());
        response.setResults(sounds);

        HttpHeaders headers = new HttpHeaders();
        headers.add("x-ratelimit-limit", (new Integer(SoundCache.getInstance().getDefaultRateLimit(apiKey)).toString()));
        headers.add("x-ratelimit-remaining", (new Integer(SoundCache.getInstance().decrementAndGet(apiKey)).toString()));
        ResponseEntity<NASAGETSoundTrackResponse> re = new ResponseEntity<NASAGETSoundTrackResponse> (response, headers, 
                HttpStatus.OK);
        return re;
    }

    private int getLimit(String limitStr) {
        int limit = 10; // default limit
        if (!StringUtils.isEmpty(limitStr)) {
            try {
                limit = Integer.parseInt(limitStr);
                if (limit < 0) {
                    limit = 10;
                }
            } catch (NumberFormatException nfe) {
                // ignore
            }
        }
        return limit;
    }

}
