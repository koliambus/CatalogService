package com.packtpub.songs.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;

public class Song {

    private static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private String id;

    @JsonProperty("author_id")
    private String authorID;

    @JsonProperty("release_date")
    private Date releaseDate;

    @JsonProperty("duration_in_seconds")
    private long durationInSeconds;

    @JsonProperty("artifact_uri")
    private String artifactUri;

    public Song() {
    }

    public Song(String id, String authorID, Date releaseDate, long durationInSeconds, String artifactUri) {
        this.id = id;
        this.authorID = authorID;
        this.releaseDate = releaseDate;
        this.durationInSeconds = durationInSeconds;
        this.artifactUri = artifactUri;
    }

    /**
     * De-serialises a song from a json payload
     * @param json the json payload to be deserialised
     * @return the equivalent song object
     *
     * Note: method throws an IllegalArgumentException, if the json was not of the right format
     */
    public static Song fromJson(final String json) {
        try {
            return OBJECT_MAPPER.readValue(json, Song.class);
        } catch (JsonParseException | JsonMappingException e) {
            throw new IllegalArgumentException("Song de-serialisation failed: " + json, e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Serialises a song to a json payload
     * @return the equivalent json string
     */
    public String toJson() {
        try {
            return OBJECT_MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
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

    public Date getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(Date releaseDate) {
        this.releaseDate = releaseDate;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Song song = (Song) o;
        return Objects.equals(id, song.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Song{" +
                "id='" + id + '\'' +
                ", authorID='" + authorID + '\'' +
                ", releaseDate=" + releaseDate +
                ", durationInSeconds=" + durationInSeconds +
                ", artifactUri='" + artifactUri + '\'' +
                '}';
    }
}
