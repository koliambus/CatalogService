package com.packtpub.config;

import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.elasticache.AmazonElastiCache;
import com.amazonaws.services.elasticache.AmazonElastiCacheClientBuilder;
import com.amazonaws.services.elasticache.model.CacheCluster;
import com.amazonaws.services.elasticache.model.CacheNode;
import com.amazonaws.services.elasticache.model.DescribeCacheClustersRequest;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.packtpub.monitoring.CloudwatchMetricsEmitter;
import com.packtpub.songs.event.PublicationNotifier;
import com.packtpub.songs.repository.SongsRepository;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Configuration
@Import(EnvironmentConfiguration.class)
@EnableWebMvc
@EnableSwagger2
@EnableCaching
public class MainConfiguration {

    private Logger logger = LoggerFactory.getLogger(MainConfiguration.class);

    private static final String STAGE_PROPERTY_NAME = "stage";
    private static final String REGION_PROPERTY_NAME = "region";

    @Autowired
    private ConfigurableEnvironment environment;


    @Bean
    public PublicationNotifier publicationNotifier(@Value("${com.packtpub.service.catalog.song.sqs-queue}") String queueName) {
        final String region = environment.getProperty(REGION_PROPERTY_NAME);

        return new PublicationNotifier(
                AmazonSQSClientBuilder.standard().
                        withRegion(region).build(),
                queueName
        );
    }

    @Bean
    public DynamoDBMapper dynamoDBMapper() {
        AmazonDynamoDB dynamoDB = getDynamoDB();

        return new DynamoDBMapper(dynamoDB);
    }

    @Bean
    public CloudwatchMetricsEmitter metricsEmitter() {
        final String region = environment.getProperty(REGION_PROPERTY_NAME);
        final String stage = environment.getProperty(STAGE_PROPERTY_NAME);

        AmazonCloudWatch cloudwatchClient = AmazonCloudWatchClientBuilder.standard()
                .withRegion(region)
                .build();

        return new CloudwatchMetricsEmitter(cloudwatchClient, stage);
    }

    @Bean
    public SongsRepository songsRepository(final DynamoDBMapper dynamoDBMapper) {
        return new SongsRepository(dynamoDBMapper);
    }

    @Bean
    public RestHighLevelClient elasticsearchClient(
            @Value("${com.packtpub.catalog.elasticsearch.host}") String host,
            @Value("${com.packtpub.catalog.elasticsearch.port}") Integer port,
            @Value("${com.packtpub.catalog.elasticsearch.scheme}") String scheme
    ) {
        return new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(host, port, scheme)
                )
        );
    }

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(PathSelectors.ant("/songs/**"))
                .build();
    }

    private AmazonDynamoDB getDynamoDB() {
        final String stage = environment.getProperty(STAGE_PROPERTY_NAME);
        final String region = environment.getProperty(REGION_PROPERTY_NAME);

        switch (stage) {
            case "dev":
                return AmazonDynamoDBClientBuilder.standard()
                        .withEndpointConfiguration(new EndpointConfiguration("http://localhost:8000", region))
                        .build();
            case "prod":
                return AmazonDynamoDBClientBuilder.standard()
                        .withRegion(region)
                        .build();
            default:
                throw new RuntimeException("Stage defined in properties unknown: " + stage);
        }
    }

    @Bean
    public LettuceConnectionFactory lettuceConnectionFactory(List<String> clusterNodes) {
        RedisClusterConfiguration clusterConfiguration = new RedisClusterConfiguration(clusterNodes);
        return new LettuceConnectionFactory(clusterConfiguration);
    }

    @Bean
    public RedisCacheConfiguration redisCacheConfiguration(@Value("${cache.ttl}") Integer ttl) {
        return RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofSeconds(ttl));
    }

    @Bean
    @Profile("prod")
    @Qualifier("clusterNodes")
    public List<String> clusterNodes(
            @Value("${" + REGION_PROPERTY_NAME + "}") String regionName,
            @Value("${cache.cluster-id}") String cacheClusterId
    ){
        AmazonElastiCache client = AmazonElastiCacheClientBuilder.standard().withRegion(regionName).build();
        DescribeCacheClustersRequest describeCacheClustersRequest = new DescribeCacheClustersRequest();
        describeCacheClustersRequest.setShowCacheNodeInfo(true);
        describeCacheClustersRequest.setCacheClusterId(cacheClusterId);
        List<CacheCluster> cacheClusterList = client.describeCacheClusters(describeCacheClustersRequest).getCacheClusters();
        if (cacheClusterList.isEmpty()) {
            logger.error("Cache cluster with id " + cacheClusterId + " cannot be found!");
        }
        List<String> nodeList = new ArrayList<>();

        for(CacheNode cacheNode : cacheClusterList.get(0).getCacheNodes()) {
            String nodeAddr = cacheNode.getEndpoint().getAddress() + ":" +cacheNode.getEndpoint().getPort();
            nodeList.add(nodeAddr);
        }

        return nodeList;
    }


    @Bean
    @Profile("dev")
    @Qualifier("clusterNodes")
    public List<String> devClusterNodes(
            @Value("${cache.host}") String host,
            @Value("${cache.port}") String port
    ){
        return Collections.singletonList(host + ":" + port);
    }
}
