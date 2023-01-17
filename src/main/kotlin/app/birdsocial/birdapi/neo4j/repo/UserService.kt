package app.birdsocial.birdapi.neo4j.repo

import app.birdsocial.birdapi.exceptions.ResourceNotFoundException
import app.birdsocial.birdapi.helper.SentryHelper
import app.birdsocial.birdapi.neo4j.schemas.UserNode
import org.neo4j.cypherdsl.core.Cypher
import org.springframework.data.neo4j.core.Neo4jTemplate
import org.springframework.stereotype.Component

@Component
class UserService(
    override val neo4j: Neo4jTemplate,
    override val sentry: SentryHelper,
) : Neo4jService<UserNode>(
    neo4j,
    sentry,
) {
    val label = "User"

    override fun findOneById(id: String): UserNode {
        sentry.span("neo4j", "findOneById") {
            val node = Cypher.node(label).named("node")
                .withProperties(mapOf(Pair("id", id)))

            val post = Cypher.node("Post").named("post")

            val request = Cypher.match(node)
//                .optionalMatch(node.relationshipTo(post, "AUTHORED"))
                .match(post)
                .returning(node, post)
                .limit(2)
                .build()

            return findOneByCypher(label, request)
        }
    }

    override fun findOneByParam(paramName: String, param: String): UserNode {
        sentry.span("neo4j", "findOneByParam") {
            val node = Cypher.node(label).named("node")
                .withProperties(mapOf(Pair(paramName, param)))

            val request = Cypher.match(node)
                .returning(node)
                .limit(2)
                .build()

            return findOneByCypher(label, request)
        }
    }

    override fun findAllByParam(paramName: String, param: String, page: Int, pageSize: Int): List<UserNode> {
        sentry.span("neo4j", "findAllByParam") {
            val node = Cypher.node(label).named("n")
                .withProperties(mapOf(Pair(paramName, param)))

            val request = Cypher.match(node)
                .returning(node)
                .skip(page * pageSize)
                .limit(pageSize)
                .build()

            return findAllByCypher(request)
        }
    }
}