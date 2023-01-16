FROM eclipse-temurin:17-jre
RUN mkdir /opt/birdapi
WORKDIR /opt/birdapi/
COPY ./build/libs/birdapi-0.0.2.jar /opt/birdapi/
EXPOSE 8080
CMD [ "java", "-jar", "./birdapi-0.0.2.jar" ]