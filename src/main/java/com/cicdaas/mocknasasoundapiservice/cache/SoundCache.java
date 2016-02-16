package com.cicdaas.mocknasasoundapiservice.cache;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

import com.cicdaas.mocknasasoundapiservice.dto.SoundTrack;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.qos.logback.classic.Logger;

public class SoundCache {

    private static final Logger LOG = (Logger) LoggerFactory.getLogger(SoundCache.class);

    private static final int DEFAULT_RATE_LIMIT = 1000;

    private static SoundCache instance;

    private List<SoundTrack> sounds;
    private Map<String, AtomicInteger> rateLimit;

    private SoundCache() {
        LOG.info("Initializing SoundCache!");
        sounds = new CopyOnWriteArrayList<SoundTrack>();
        rateLimit = new ConcurrentHashMap<String, AtomicInteger>();
        loadSoundTracks();
        LOG.info("SoundCache initialization completed successfully!");
    }

    public static SoundCache getInstance() {
        if (instance == null) {
            instance = new SoundCache();
        }
        return instance;
    }

    public List<SoundTrack> getAll(int limit) {
        return sounds.stream().limit(limit).collect(Collectors.toList());
    }

    public List<SoundTrack> filter(String keyword, int limit) {
        return sounds.stream().filter((st) -> st.getTagList().equals(keyword)).limit(limit).collect(Collectors.toList());
    }

    public int getDefaultRateLimit(String apiKey) {
        return DEFAULT_RATE_LIMIT;
    }

    public int decrementAndGet(String apiKey) {
        if (!rateLimit.containsKey(apiKey)) {
            rateLimit.put(apiKey, new AtomicInteger(DEFAULT_RATE_LIMIT));
        }
        return rateLimit.get(apiKey).decrementAndGet();
    }

    private void loadSoundTracks() {
        InputStream in = null;
        String soundTrackFileName = "/sound_tracks.json";
        try {
            in = this.getClass().getResourceAsStream(soundTrackFileName);
            ObjectMapper mapper = new ObjectMapper();
            sounds =  mapper.readValue(in, new TypeReference<List<SoundTrack>>(){});
            LOG.debug("Loaded sound tracks - size: " + sounds.size());
        } catch (Exception e) {
            throw new RuntimeException(String.format("Error occurred while loading sound tracks! File: %s", soundTrackFileName), 
                    e);
        }
    }

}
