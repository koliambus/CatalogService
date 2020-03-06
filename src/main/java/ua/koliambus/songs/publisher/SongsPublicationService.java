package ua.koliambus.songs.publisher;

import ua.koliambus.songs.event.PublicationNotifier;
import ua.koliambus.songs.model.Song;
import ua.koliambus.songs.repository.SongsRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SongsPublicationService {

    private final SongsRepository songsRepository;

    private final PublicationNotifier publicationNotifier;

    public SongsPublicationService(SongsRepository songsRepository, PublicationNotifier publicationNotifier) {
        this.songsRepository = songsRepository;
        this.publicationNotifier = publicationNotifier;
    }

    public Optional<Song> getSong(String songIdentifier) {
        return songsRepository.getSong(songIdentifier);
    }

    public void publish(Song song) {
        songsRepository.storeSong(song);
        publicationNotifier.onSongPublished(song);
    }


}
