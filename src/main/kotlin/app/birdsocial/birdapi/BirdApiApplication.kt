package app.birdsocial.birdapi

import org.neo4j.driver.Driver
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.data.neo4j.core.DatabaseSelectionProvider
import org.springframework.data.neo4j.core.ReactiveDatabaseSelectionProvider
import org.springframework.data.neo4j.core.transaction.Neo4jTransactionManager
import org.springframework.data.neo4j.core.transaction.ReactiveNeo4jTransactionManager


@SpringBootApplication
@EnableAutoConfiguration(exclude = [
//    SecurityAutoConfiguration::class,
    HibernateJpaAutoConfiguration::class
])

class BirdApiApplication {}

fun main(args: Array<String>) {
    runApplication<BirdApiApplication>(*args)
}
