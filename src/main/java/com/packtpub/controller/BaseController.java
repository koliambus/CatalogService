package com.packtpub.controller;

import com.packtpub.songs.model.Song;
import com.packtpub.songs.publisher.SongsPublicationService;
import com.packtpub.songs.repository.SongIdentifierExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class BaseController {

    @Autowired
    private SongsPublicationService publicationService;
//
//    @RequestMapping(value = "/songs",
//            method = RequestMethod.GET,
//            produces = MediaType.APPLICATION_JSON_VALUE)
//    public ResponseEntity<List<Song>> getSongs(@RequestParam(value = "limit", defaultValue = "10") Integer limit,
//                                               @RequestParam(value = "offset", defaultValue = "0") Integer offset) {
//        List<Song> songs = publicationService.getSongs(offset, limit);
//        if (songs.isEmpty()) {
//            return ResponseEntity.noContent().build();
//        }
//
//        return ResponseEntity.ok(songs);
//    }

    @RequestMapping(value = "songs/{song_id}",
            method = RequestMethod.GET,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Song> getSong(@PathVariable("song_id") String songIdentifier, ModelMap model) {
        final Optional<Song> song = publicationService.getSong(songIdentifier);
        return song.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());

    }

    @RequestMapping(value = {"/songs"},
            method = RequestMethod.POST,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> publishSong(@RequestBody Song song) {
        try {
            publicationService.publish(song);

            return ResponseEntity.ok().build();
        } catch(SongIdentifierExistsException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
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