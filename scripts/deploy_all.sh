#!/bin/bash

PROJECT_HOME=$HOME/IdeaProjects/komplexni-validator-web-backend
TOMCAT_HOME=$HOME/Software/tomcat

echo "building"
cd $PROJECT_HOME/services-java
./gradlew build

services=(
"job-execution-service"
"notification-service"
"quota-service"
"result-service"
"upload-service"
"user-service"
"validation-manager-service"
)


#iterate services and deploys them to tomcat
for service in "${services[@]}"; do
  echo "deploying $service"
  cp $PROJECT_HOME/services-java/$service/build/libs/kv-$service.war $TOMCAT_HOME/webapps/kv-$service.war
  sleep 5
done
