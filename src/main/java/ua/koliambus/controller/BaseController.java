package ua.koliambus.controller;

import ua.koliambus.monitoring.CloudwatchMetricsEmitter;
import ua.koliambus.songs.model.Song;
import ua.koliambus.songs.publisher.SongsPublicationService;
import ua.koliambus.songs.repository.SongIdentifierExistsException;
import ua.koliambus.songs.search.SongsSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class BaseController {

    private static final String SERVICE_NAMESPACE = "CatalogService";

    private static final String ENDPOINT_LATENCY_METRIC_NAME_TEMPLATE = "%s-Latency";
    private static final String ENDPOINT_REQUESTS_METRIC_NAME_TEMPLATE = "%s-Requests";
    private static final String SONG_EXISTS_METRIC_NAME = "SongExists";

    private static final String GET_SONG_ENDPOINT_NAME = "GetSong";
    private static final String PUBLISH_SONG_ENDPOINT_NAME = "PublishSong";

    @Autowired
    private SongsPublicationService publicationService;

    @Autowired
    private SongsSearchService searchService;

    @Autowired
    private CloudwatchMetricsEmitter metricsEmitter;

    @GetMapping(value = "songs/{song_id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Cacheable(cacheNames = {"get_song"}, key = "#songIdentifier")
    public ResponseEntity<Song> getSong(@PathVariable("song_id") String songIdentifier) {
        emitEndpointRequest(GET_SONG_ENDPOINT_NAME);
        final long startTimestamp = System.currentTimeMillis();
        final Optional<Song> song = publicationService.getSong(songIdentifier);
        ResponseEntity<Song> songResponseEntity = song.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
        emitLatencyMetric(GET_SONG_ENDPOINT_NAME, System.currentTimeMillis() - startTimestamp);
        return songResponseEntity;
    }

    @GetMapping(value = "songs", produces = MediaType.APPLICATION_JSON_VALUE)
    @Cacheable(cacheNames = {"songs_search_result"}, key = "#q")
    public ResponseEntity<List<Song>> searchSongs(@RequestParam(name = "q", defaultValue = "") String q) {
        List<Song> songs = searchService.searchSongs(q);

        if (songs.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(songs);
    }

    @PostMapping(value = {"/songs"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @CacheEvict(cacheNames = {"songs_search_result"}, allEntries = true)
    public ResponseEntity<?> publishSong(@RequestBody Song song) {
        emitEndpointRequest(PUBLISH_SONG_ENDPOINT_NAME);
        final long startTimestamp = System.currentTimeMillis();
        try {
            publicationService.publish(song);

            return ResponseEntity.ok().build();
        } catch(SongIdentifierExistsException e) {
            metricsEmitter.emitMetric(SERVICE_NAMESPACE, SONG_EXISTS_METRIC_NAME, 1);
            return ResponseEntity.badRequest().body(e.getMessage());
        } finally {
            emitLatencyMetric(PUBLISH_SONG_ENDPOINT_NAME, System.currentTimeMillis() - startTimestamp);
        }
    }


    @GetMapping(value = "health-check", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Instance is healthy!");
    }

    private void emitLatencyMetric(final String endpointName, final long value) {
        final String metricName = String.format(ENDPOINT_LATENCY_METRIC_NAME_TEMPLATE, endpointName);
        metricsEmitter.emitMetric(SERVICE_NAMESPACE, metricName, value);
    }

    private void emitEndpointRequest(final String endpointName) {
        final String metricName = String.format(ENDPOINT_REQUESTS_METRIC_NAME_TEMPLATE, endpointName);
        metricsEmitter.emitMetric(SERVICE_NAMESPACE, metricName, 1.0);
    }

    static class Message {
        String message;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}