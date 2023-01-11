package app.birdsocial.birdapi.config

import org.neo4j.ogm.config.Configuration
import org.neo4j.ogm.session.SessionFactory
import org.springframework.context.annotation.Bean

@org.springframework.context.annotation.Configuration
class Neo4jConfiguration(val envData: EnvironmentData) {
    @Bean
    fun getConfiguration(): Configuration {

        return Configuration.Builder()
//            .uri(File("target/graph.db").toURI().toString()) // For Embedded
                .uri(envData.getData("NEO4J_URI"))
                .credentials(
                    envData.getData("NEO4J_USERNAME"),
                    envData.getData("NEO4J_PASSWORD")
                )
                .build()
    }

    @Bean
    fun getSessionFactory(config: Neo4jConfiguration): SessionFactory {
        return SessionFactory(config.getConfiguration(), "app.birdsocial.birdapi.neo4j.schemas")
    }
}
