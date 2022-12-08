package app.birdsocial.birdapi.neo4j

import app.birdsocial.birdapi.BirdApiApplication
import org.neo4j.ogm.config.Configuration
import org.neo4j.ogm.session.SessionFactory
import org.springframework.context.annotation.Bean

@org.springframework.context.annotation.Configuration
class Neo4jConfiguration {
    @Bean
    fun getConfiguration(): Configuration {
        return Configuration.Builder()
                .uri(BirdApiApplication.dotenv["NEO4J_URI"])
                .credentials(
                        BirdApiApplication.dotenv["NEO4J_USERNAME"],
                        BirdApiApplication.dotenv["NEO4J_PASSWORD"]
                )
                .build()
    }

    @Bean
    fun getSessionFactory(config: Neo4jConfiguration): SessionFactory {
        return SessionFactory(config.getConfiguration(), "app.birdsocial.birdapi.neo4j.schemas")
    }
}
