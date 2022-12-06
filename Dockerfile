FROM eclipse-temurin:17-jre
RUN mkdir /opt/birdapi
COPY ./build/libs/birdapi-0.0.1-SNAPSHOT.jar /opt/birdapi
#CMD [ "bash" ]
CMD [ "java", "-jar", "/opt/birdapi/birdapi-0.0.1-SNAPSHOT.jar" ]