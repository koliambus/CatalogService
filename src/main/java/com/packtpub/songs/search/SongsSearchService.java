package com.packtpub.songs.search;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.packtpub.songs.model.Song;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class SongsSearchService {

    private final RestHighLevelClient restHighLevelClient;

    public SongsSearchService(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }

    public List<Song> searchSongs(String q) {
        List<Song> songs = new ArrayList<>();
        SearchRequest searchRequest = new SearchRequest("published-songs");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        if (q.isEmpty()) {
            searchSourceBuilder.query(QueryBuilders.matchAllQuery());
        } else {
            // TODO fix query
            searchSourceBuilder.query(QueryBuilders.wildcardQuery("id", q + "*"));
        }

        searchRequest.source(searchSourceBuilder);
        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            for (SearchHit hit : searchResponse.getHits()) {
                songs.add(new ObjectMapper().readValue(hit.getSourceAsString(), Song.class));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return songs;
    }
}
