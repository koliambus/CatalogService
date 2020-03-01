package com.packtpub.songs.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.packtpub.songs.model.Song;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
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

        if (!q.isEmpty()) {
            Request request = new Request("GET", "/published-songs/_search");
            request.addParameter("q", q);
            try {
                Response response = restHighLevelClient.getLowLevelClient().performRequest(request);
                String string = EntityUtils.toString(response.getEntity());
                JsonNode root = new ObjectMapper().readTree(string);
                for (JsonNode songWraper : root.at("/hits/hits")) {
                    Song song = new ObjectMapper().readValue(songWraper.at("/_source").toString(), Song.class);
                    songs.add(song);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {

            SearchRequest searchRequest = new SearchRequest("published-songs");
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

            searchSourceBuilder.query(QueryBuilders.matchAllQuery());

            searchRequest.source(searchSourceBuilder);
            try {
                SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
                for (SearchHit hit : searchResponse.getHits()) {
                    songs.add(new ObjectMapper().readValue(hit.getSourceAsString(), Song.class));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return songs;
    }
}
