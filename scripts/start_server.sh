#!/bin/bash
cd /home/ec2-user/catalog-service
java -jar CatalogService.jar --spring.profiles.active=prod > /dev/null 2>&1 &
