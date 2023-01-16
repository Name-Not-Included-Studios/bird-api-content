package app.birdsocial.birdapi.neo4j.repo

import app.birdsocial.birdapi.exceptions.BirdException
import app.birdsocial.birdapi.exceptions.ResourceNotFoundException
import app.birdsocial.birdapi.helper.SentryHelper
import org.neo4j.cypherdsl.core.Cypher
import org.neo4j.cypherdsl.core.Statement
import org.springframework.data.neo4j.core.Neo4jTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
class Neo4jRepository(
    val neo4j: Neo4jTemplate,
    val sentry: SentryHelper,
) {
    final inline fun <reified T> findOneById(label: String, id: String): T {
        sentry.span("neo4j", "findOneById") {
            val node = Cypher.node(label).named("n")
                .withProperties(mapOf(Pair("id", id)))

            val request = Cypher.match(node)
                .returning(node)
                .limit(2)
                .build()

            return findOneByCypher<T>(label, request) ?: throw ResourceNotFoundException(label)
        }
    }

    final inline fun <reified T> findOneByParam(label: String, paramName: String, param: String): T {
        sentry.span("neo4j", "findOneByParam") {
            val node = Cypher.node(label).named("n")
                .withProperties(mapOf(Pair(paramName, param)))

            val request = Cypher.match(node)
                .returning(node)
                .limit(2)
                .build()

            return findOneByCypher<T>(label, request)
        }
    }

    final inline fun <reified T> findAllByParam(label: String, paramName: String, param: String, page: Int, pageSize: Int): List<T> {
        sentry.span("neo4j", "findOneByParam") {
            val node = Cypher.node(label).named("n")
                .withProperties(mapOf(Pair(paramName, param)))

            val request = Cypher.match(node)
                .returning(node)
                .skip(page * pageSize)
                .limit(pageSize)
                .build()

            return findAllByCypher<T>(request)
        }
    }

    final inline fun <reified T> findOneByCypher(dataName: String, cypher: Statement): T {
        sentry.span("neo4j", "findOneByCypher") {
            println("Cypher: ${cypher.cypher}")

            val users = neo4j.findAll(cypher, T::class.java)

            // Make sure every user is of correct type and is not null
            for (user in users)
                if (user !is T || user == null)
                    throw BirdException("$dataName returned wrong type")


            if (users.size > 1)
                throw BirdException("Ambiguous Data Returned")

            if (users.size < 1)
                throw ResourceNotFoundException(dataName)

            return users[0]
        }
    }

    final inline fun <reified T> findAllByCypher(cypher: Statement): List<T> {
        sentry.span("neo4j", "findAllByCypher") {
            println("Cypher: ${cypher.cypher}")

            val users = neo4j.findAll(cypher, T::class.java)

            return users
        }
    }
}