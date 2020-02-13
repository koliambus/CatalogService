package com.packtpub.songs.repository.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import com.packtpub.songs.model.Song;

import java.util.Date;

@DynamoDBTable(tableName = "songs")
public class DynamoDBSongItem {

    @DynamoDBHashKey(attributeName = "id")
    private String id;

    @DynamoDBAttribute(attributeName = "author_id")
    private String authorID;

    @DynamoDBAttribute(attributeName = "release_date_in_epoch_millis")
    private long releaseDateInEpochMillis;

    @DynamoDBAttribute(attributeName = "duration_in_seconds")
    private long durationInSeconds;

    @DynamoDBAttribute(attributeName = "artifact_uri")
    private String artifactUri;

    public static DynamoDBSongItem fromSong(final Song song) {
        final DynamoDBSongItem dynamoDBSongItem = new DynamoDBSongItem();
        dynamoDBSongItem.setId(song.getId());
        dynamoDBSongItem.setAuthorID(song.getAuthorID());
        dynamoDBSongItem.setDurationInSeconds(song.getDurationInSeconds());
        dynamoDBSongItem.setReleaseDateInEpochMillis(song.getReleaseDate().getTime());
        dynamoDBSongItem.setArtifactUri(song.getArtifactUri());

        return dynamoDBSongItem;
    }

    public Song toSong() {
        return new Song(
                this.id,
                this.authorID,
                new Date(this.releaseDateInEpochMillis),
                this.durationInSeconds,
                this.artifactUri);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthorID() {
        return authorID;
    }

    public void setAuthorID(String authorID) {
        this.authorID = authorID;
    }

    public long getReleaseDateInEpochMillis() {
        return releaseDateInEpochMillis;
    }

    public void setReleaseDateInEpochMillis(long releaseDateInEpochMillis) {
        this.releaseDateInEpochMillis = releaseDateInEpochMillis;
    }

    public long getDurationInSeconds() {
        return durationInSeconds;
    }

    public void setDurationInSeconds(long durationInSeconds) {
        this.durationInSeconds = durationInSeconds;
    }

    public String getArtifactUri() {
        return artifactUri;
    }

    public void setArtifactUri(String artifactUri) {
        this.artifactUri = artifactUri;
    }
}
