# Catalog Service

## Project overview

Based on Spring Boot framework. It exposes REST API where user can publish the new song, get a song by id and search songs. 

#### Publish

Published songs are stored to AWS DynamoDB, and also are put to AWS SQS queue.
 
Another service reads a message from SQS and indexes it with AWS Elasticsearch Service.

#### Search
 
To search songs Catalog service uses mentioned Elasticsearch index. 

#### Read

Getting songs by id is made directly from Dynamo DB.

## Application features

#### Unit tests

Testing with Junit and Mockito

#### Monitoring

Application uses CloudWatch with Micrometer to look after system and application metrics.

#### Documentation

Swagger V2 is used to provide documentation that can be swallowed to use service.

#### Caching

Centralized caching approach is used with Redis placed in AWS Elasticache
 
#### Logging 

Logging is provided by Logback.

## Infrastructure

#### CI/CD

Leverage AWS CodePipeline to Build and Deploy application. 

On each push to GitHub, sourcecode is pulled by AWS CodeBuild where a runnable Spring Boot jar artifact is created with Maven build. Artifact is put to AWS S3. AWS CodeDeploy deploys artifact to EC2 instances using Green-Blue deployment methodology.

#### Load balancing

In order to easy scale up an application, AWS ELB provides a singe point on access to all EC2 instances.

#### Scaling

Auto scaling group was configured to monitor deployed instances to have moderate CPU usage and another instance is up when the high threshold is exceeded. When the low CPU threshold is exceeded, one of the instances terminates to save resources and money. Alarms are configured in AWS CloudWatch. 
