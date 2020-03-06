package ua.koliambus.songs.event;

import com.amazonaws.services.sqs.AmazonSQS;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ua.koliambus.songs.model.Song;
import org.springframework.beans.factory.annotation.Value;

public class PublicationNotifier {

    private final AmazonSQS amazonSQS;

    private final String queueName;

    public PublicationNotifier(AmazonSQS amazonSQS,
                               @Value("${ua.koliambus.service.catalog.song.sqs-queue}") String queueName) {
        this.amazonSQS = amazonSQS;
        this.queueName = queueName;
    }

    public void onSongPublished(Song song) {
        try {
            amazonSQS.sendMessage(queueName, new ObjectMapper().writeValueAsString(song));
        } catch (JsonProcessingException e) {
            // TODO handle properly
            e.printStackTrace();
        }
    }
}
