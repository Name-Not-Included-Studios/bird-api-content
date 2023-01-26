package app.birdsocial.birdapi.repository

import app.birdsocial.birdapi.exceptions.BirdException
import app.birdsocial.birdapi.exceptions.ResourceNotFoundException
import app.birdsocial.birdapi.helper.SentryHelper
import app.birdsocial.birdapi.neo4j.schemas.PostNode
import org.neo4j.cypherdsl.core.Cypher
import org.neo4j.cypherdsl.core.Statement
import org.springframework.data.neo4j.core.Neo4jClient
import org.springframework.data.neo4j.core.Neo4jTemplate
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.stereotype.Component

@Component
class PostService(
     val neo4j: Neo4jTemplate,
     val neo4jClient: Neo4jClient,
     val sentry: SentryHelper,
) {
    val label = "Post"

     fun findOneById(id: String): PostNode {
        sentry.span("neo4j", "findOneById") {
            val node = Cypher.node(label).named("n")
                .withProperties(mapOf(Pair("id", id)))

            val request = Cypher.match(node)
                .returning(node)
                .limit(2)
                .build()

            return findOneByCypher(label, request)
        }
    }

//     fun findOneByParam(paramName: String, param: String): PostNode {
//        sentry.span("neo4j", "findOneByParam") {
//            val node = Cypher.node(label).named("n")
//                .withProperties(mapOf(Pair(paramName, param)))
//
//            val request = Cypher.match(node)
//                .returning(node)
//                .limit(2)
//                .build()
//
//            return findOneByCypher(label, request)
//        }
//    }
//
//     fun findAllByParam(paramName: String, param: String, page: Int, pageSize: Int): List<PostNode> {
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
//            return findAllByCypher(request)
//        }
//    }

    private inline fun <reified R> findOneByCypher(dataName: String, cypher: Statement): R {
        sentry.span("neo4j", "findOneByCypher") {
            println("Cypher: ${cypher.cypher}")

            val nodes = neo4j.findAll(cypher, R::class.java)

            // Make sure every user is of correct type and is not null
            for (node in nodes)
                if (node !is R || node == null)
                    throw BirdException("$dataName returned wrong type")


            if (nodes.size > 1)
                throw BirdException("Ambiguous Data Returned")

            if (nodes.size < 1)
                throw ResourceNotFoundException(dataName)

            return nodes[0]
        }
    }

    private inline fun <reified R> findAllByCypher(cypher: Statement): List<R> {
        sentry.span("neo4j", "findAllByCypher") {
            println("Cypher: ${cypher.cypher}")

            return neo4j.findAll(cypher, R::class.java)
        }
    }
}