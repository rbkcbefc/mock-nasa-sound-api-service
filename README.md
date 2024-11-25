# Introduction

In QA Environment, configure your application to use this Mock Service (instead of real NASA Sound API). Based on API Key, we can return various types of canned response to verify application behavior. For eg, Stream URL is empty etc which is hard to simulate with real service. Java Concurrency - AutomicInteger is used for consistent 'limit-remaining' header. 

# Technologies

- Java 8 (Stream API is used to filter & limit sound track records) 
- Spring MVC
- Jetty Embedded Servlet Container

# Running the Service

- clone the repo
- mvn clean package
- mvn jetty:run

The mock service will be up & running on port: 8080

# Verify the Service

- Open new terminal window
- curl http://localhost:8080/planetary/sounds?api_key=123&limit=1

Response
````
{"count":1,"results":[{"description":"The Voyager 1 spacecraft has experienced three \"tsunami waves\" in interstellar space. Listen to how these waves cause surrounding ionized matter to ring. More details on this sound can be found here: http://www.nasa.gov/jpl/nasa-voyager-tsunami-wave-still-flies-through-interstellar-space/","license":"cc-by-nc-sa","title":"Voyager 1: Three \"Tsunami Waves\" in Interstellar Space","duration":18365,"id":181835738,"download_url":"https://api.soundcloud.com/tracks/181835738/download","last_modified":"2014/12/16 22:34:23 +0000","stream_url":"https://api.soundcloud.com/tracks/181835738/stream","tag_list":"space"}]}
````

# API Test Automation

- Link:  https://github.com/cicdaas/nasa-sound-api-automation

# Github Actions are configured to build Docker Image and Push to ECR Repo
~ In the github repository settings, 
https://github.com/rbkcbefc/mock-nasa-sound-api-service/settings/secrets/actions

add four secrets
! AWS_ACCESS_KEY_ID
! AWS_SECRET_ACCESS_KEY
! AWS_REGION
! REPO_NAME 

Eg: REPO_NAME = mock-nasa-sound-api-service

To test locally,
docker images
mvn clean package
docker build -t mock-nasa-sound-api-service .
docker images

aws ecr get-login-password --region us-east-1 | docker login --username AWS --password-stdin <<ecr_id>>>.dkr.ecr.us-east-1.amazonaws.com

docker push <<ecr_id>>>.dkr.ecr.us-east-1.amazonaws.com/mock-nasa-sound-api-service:latest

