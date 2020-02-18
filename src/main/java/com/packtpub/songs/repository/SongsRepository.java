package com.packtpub.songs.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.*;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.packtpub.songs.model.Song;
import com.packtpub.songs.repository.dynamodb.DynamoDBSongItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class SongsRepository {

    private final DynamoDBMapper dynamoDBMapper;

    public SongsRepository(final DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    public Optional<Song> getSong(final String songIdentifier) {
        DynamoDBSongItem songItem = dynamoDBMapper.load(DynamoDBSongItem.class, songIdentifier);

        if (songItem == null) {
            return Optional.empty();
        }

        return Optional.of(songItem.toSong());
    }

    public List<Song> getSongs(Integer offset, Integer limit) {
        DynamoDBQueryExpression<DynamoDBSongItem> queryExpression = new DynamoDBQueryExpression<>();
        queryExpression.setLimit(limit);
        PaginatedQueryList<DynamoDBSongItem> songItem = dynamoDBMapper.query(DynamoDBSongItem.class, queryExpression);

        return songItem.parallelStream().map(DynamoDBSongItem::toSong).collect(Collectors.toList());
    }

    public void storeSong(final Song song) {
        final DynamoDBSongItem dynamoDBSongItem = DynamoDBSongItem.fromSong(song);
        final DynamoDBSaveExpression dynamoDBSaveExpression = getSongIdDoesNotExistExpression();

        try {
            dynamoDBMapper.save(dynamoDBSongItem, dynamoDBSaveExpression);
        } catch (ConditionalCheckFailedException e) {
            throw new SongIdentifierExistsException(song.getId());
        }
    }

    private DynamoDBSaveExpression getSongIdDoesNotExistExpression() {
        DynamoDBSaveExpression samePartitionIdExistsExpression = new DynamoDBSaveExpression();
        Map<String, ExpectedAttributeValue> expected = new HashMap<>();
        expected.put("id", new ExpectedAttributeValue(false));
        samePartitionIdExistsExpression.setExpected(expected);

        return samePartitionIdExistsExpression;
    }
}
