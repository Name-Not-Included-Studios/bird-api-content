package app.birdsocial.birdapi.config

import org.neo4j.driver.AuthTokens
import org.neo4j.driver.Driver
import org.neo4j.driver.GraphDatabase
import org.springframework.context.annotation.Bean
import org.springframework.data.neo4j.core.DatabaseSelectionProvider
import org.springframework.data.neo4j.core.transaction.Neo4jTransactionManager

@org.springframework.context.annotation.Configuration
class Neo4jConfiguration() {
    @Bean
    fun transactionManager(
        driver: Driver,
        databaseNameProvider: DatabaseSelectionProvider?
    ): Neo4jTransactionManager? {
        return Neo4jTransactionManager(driver, databaseNameProvider!!)
    }

//    @Bean
//    fun reactiveTransactionManager(
//        driver: Driver,
//        databaseNameProvider: ReactiveDatabaseSelectionProvider?
//    ): ReactiveNeo4jTransactionManager? {
//        return ReactiveNeo4jTransactionManager(driver, databaseNameProvider!!)
//    }

//    @Bean
//    fun getConfiguration(): Driver {
//
//        return GraphDatabase.driver(
////            .uri(File("target/graph.db").toURI().toString()) // For Embedded
//                envData.getData("NEO4J_URI"),
//                AuthTokens.basic(
//                    envData.getData("NEO4J_USERNAME"),
//                    envData.getData("NEO4J_PASSWORD")
//                )
//        )
//    }
}
