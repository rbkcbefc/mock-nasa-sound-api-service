FROM tomcat:9.0
ADD target/mocknasasoundapiservice.war /usr/local/tomcat/webapps/mock-nasa-sound-api.war

EXPOSE 8080

