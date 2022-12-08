package app.birdsocial.birdapi.neo4j

import org.neo4j.ogm.config.Configuration
import org.neo4j.ogm.session.Session
import org.neo4j.ogm.session.SessionFactory

object Neo4jSessionFactory {
    val configuration: Configuration = Configuration.Builder()
        .uri("bolt://localhost")
        .credentials("neo4j", "password")
        .build()

    val sessionFactory: SessionFactory = SessionFactory(configuration, "app.birdsocial.birdapi.neo4j.schemas")

//    private val factory: Neo4jSessionFactory = this

//    fun getInstance(): Neo4jSessionFactory? {
//        return factory
//    }

    fun getNeo4jSession(): Session? {
        return sessionFactory.openSession()
    }
}

//class Neo4jSessionFactoryOld {
//    private val configuration: Configuration = Configuration.Builder()
//        .uri("bolt://localhost")
//        .credentials("neo4j", "password")
//        .build()
//
//    private val sessionFactory: SessionFactory = SessionFactory(configuration, "app.birdsocial.birdapi.neo4j.schemas")
//    private val factory: Neo4jSessionFactory = Neo4jSessionFactory()
//
//    fun getInstance(): Neo4jSessionFactory? {
//        return factory
//    }
//
//    // prevent external instantiation
//    private fun Neo4jSessionFactory(): Neo4jSessionFactory {}
//
//    fun getNeo4jSession(): Session? {
//        return sessionFactory.openSession()
//    }
//}