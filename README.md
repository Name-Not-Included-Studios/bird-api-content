# java-bird-api

---

API for the birdsocial project

## Usage

There are a few ways to run the project

### Docker (Preferred)

This is our preferred way of distribution and running. \
If you have [Docker](https://docs.docker.com/get-docker/) installed, all you have to do is run:

> `docker run -d thatcoldtoast/birdapi:latest`

Then navigate to `http://172.17.0.2:8080/graphiql`

### Gradle

This is the easiest way if you don't have docker installed \
All you have to do is just run the Gradle task `bootRun` \
This can be accomplished by using the gradlew files as follows:

> **Linux** > `./gradlew bootRun`

> **Windows** > `.\gradlew.bat bootRun`

### Jarfile

A good way to package this is by generating a jarfile\
You can do this with the Gradle task `jar` \
then you can distribute that jarfile amd/or run `java -jar ./build/libs/birdapi-(version)jar` \
where you replace `(version)` with the specified version
This can be accomplished by using the gradlew files as follows:

**IMPORTANT** : Replace `(version)` with the correct jarfile name !

> **Linux** > `./gradlew jar` > `java -jar ./build/libs/birdapi-(version).jar`

> **Windows** > `.\gradlew.bat jar` > `java -jar ./build/libs/birdapi-(version).jar`
