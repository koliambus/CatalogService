package com.packtpub.config;

import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.packtpub.songs.event.PublicationNotifier;
import com.packtpub.songs.repository.SongsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@Configuration
@Import(EnvironmentConfiguration.class)
@EnableWebMvc
public class MainConfiguration {

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
    public SongsRepository songsRepository(final DynamoDBMapper dynamoDBMapper) {
        return new SongsRepository(dynamoDBMapper);
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
}
