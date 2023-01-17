package app.birdsocial.birdapi.neo4j.repo

import app.birdsocial.birdapi.exceptions.BirdException
import app.birdsocial.birdapi.exceptions.ResourceNotFoundException
import app.birdsocial.birdapi.helper.SentryHelper
import org.neo4j.cypherdsl.core.Statement
import org.springframework.data.neo4j.core.Neo4jTemplate
import org.springframework.stereotype.Component

@Component
abstract class Neo4jService<T>(
    val neo4j: Neo4jTemplate,
    val sentry: SentryHelper,
) {
    abstract fun findOneById(id: String): T
    abstract fun findOneByParam(paramName: String, param: String): T

    abstract fun findAllByParam(
        paramName: String,
        param: String,
        page: Int,
        pageSize: Int
    ): List<T>

//    final inline fun <reified T> findOneById(label: String, id: String): T {
//        sentry.span("neo4j", "findOneById") {
//            val node = Cypher.node(label).named("n")
//                .withProperties(mapOf(Pair("id", id)))
//
//            val request = Cypher.match(node)
//                .returning(node)
//                .limit(2)
//                .build()
//
//            return findOneByCypher<T>(label, request) ?: throw ResourceNotFoundException(label)
//        }
//    }

//    final inline fun <reified T> findOneByParam(label: String, paramName: String, param: String): T {
//        sentry.span("neo4j", "findOneByParam") {
//            val node = Cypher.node(label).named("n")
//                .withProperties(mapOf(Pair(paramName, param)))
//
//            val request = Cypher.match(node)
//                .returning(node)
//                .limit(2)
//                .build()
//
//            return findOneByCypher<T>(label, request)
//        }
//    }

//    final inline fun <reified T> findAllByParam(
//        label: String,
//        paramName: String,
//        param: String,
//        page: Int,
//        pageSize: Int
//    ): List<T> {
//        sentry.span("neo4j", "findAllByParam") {
//            val node = Cypher.node(label).named("n")
//                .withProperties(mapOf(Pair(paramName, param)))
//
//            val request = Cypher.match(node)
//                .returning(node)
//                .skip(page * pageSize)
//                .limit(pageSize)
//                .build()
//
//            return findAllByCypher<T>(request)
//        }
//    }

    final inline fun <reified R> findOneByCypher(dataName: String, cypher: Statement): R {
        sentry.span("neo4j", "findOneByCypher") {
            println("Cypher: ${cypher.cypher}")

            val nodes = neo4j.findAll(cypher, R::class.java)

            // Make sure every user is of correct type and is not null
            for (node in nodes)
                if (node !is R || node == null)
                    throw BirdException("$dataName returned wrong type")


//            if (users.size > 1)
//                throw BirdException("Ambiguous Data Returned")

            if (nodes.size < 1)
                throw ResourceNotFoundException(dataName)

            return nodes[0]
        }
    }

    final inline fun <reified R> findAllByCypher(cypher: Statement): List<R> {
        sentry.span("neo4j", "findAllByCypher") {
            println("Cypher: ${cypher.cypher}")

            return neo4j.findAll(cypher, R::class.java)
        }
    }
}