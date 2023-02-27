package app.birdsocial.birdapi.graphql.resolvers

import app.birdsocial.birdapi.config.ApplicationConfig
import app.birdsocial.birdapi.exceptions.BirdException
import app.birdsocial.birdapi.graphql.types.*
import app.birdsocial.birdapi.neo4j.schemas.UserNode
import app.birdsocial.birdapi.repository.UserService
import org.springframework.data.neo4j.core.Neo4jTemplate
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping

class SearchResolver(
        val appConfig: ApplicationConfig,
        val userService: UserService,
        val neo4j: Neo4jTemplate,
//        val sentry: SentryHelper,
) {
    // TODO - Unprotected
    @QueryMapping
    fun searchUsers(@Argument query: UserSearch): List<User> {
        val pagination = Pagination(query.pagination.page, query.pagination.pageSize.coerceIn(1, appConfig.maxPageSize))

        when (query.operation) {
            Operation.EQUALS -> when (query.param) {
                UserSearchParam.USERNAME -> return userService.findAllByParam("username", query.value, pagination)
                        .map { user -> user.toUser() }

                UserSearchParam.DISPLAY_NAME -> return userService.findAllByParam("displayName", query.value, pagination)
                        .map { user -> user.toUser() }

                UserSearchParam.BIO -> return userService.findAllByParam("bio", query.value, pagination)
                        .map { user -> user.toUser() }

                UserSearchParam.WEB_URL -> return userService.findAllByParam("websiteUrl", query.value, pagination)
                        .map { user -> user.toUser() }

                UserSearchParam.CHIRP_COUNT -> {
                    val params = mutableMapOf<String, Any>(Pair("value", query.value))
                    val cypher =
                            """
                            MATCH (u:User)-[r:AUTHORED]->(:Post)
                            WITH u, count(r) as postCount
                            WHERE postCount = ${'$'}value
                            RETURN u $pagination
                        """.trimIndent()
                    return neo4j.findAll(cypher, params, UserNode::class.java).map { user -> user.toUser() }
                }

                UserSearchParam.FOLLOWER_COUNT -> {
                    val params = mutableMapOf<String, Any>(Pair("value", query.value))
                    val cypher =
                            """
                            MATCH (:User)-[r:FOLLOWING]->(u:User)
                            WITH u, count(r) as followCount
                            WHERE followCount = ${'$'}value
                            RETURN u $pagination
                        """.trimIndent()
                    return neo4j.findAll(cypher, params, UserNode::class.java).map { user -> user.toUser() }
                }

                UserSearchParam.FOLLOWING_COUNT -> {
                    val params = mutableMapOf<String, Any>(Pair("value", query.value))
                    val cypher =
                            """
                            MATCH (u:User)-[r:FOLLOWING]->(:User)
                            WITH u, count(r) as followCount
                            WHERE followCount = ${'$'}value
                            RETURN u $pagination
                        """.trimIndent()
                    return neo4j.findAll(cypher, params, UserNode::class.java).map { user -> user.toUser() }
                }

                else -> throw BirdException("Invalid Query Param")
            }

            Operation.CONTAINS -> when (query.param) {
                UserSearchParam.USERNAME -> {
                    if (query.value.isEmpty()) throw BirdException("Query length must be greater than 0.")
                    val params = mutableMapOf<String, Any>(Pair("value", query.value))
                    val cypher =
                            """
                            MATCH (u:User)
                            WHERE u.username CONTAINS ${'$'}value
                            RETURN u $pagination
                        """.trimIndent()

                    return neo4j.findAll(cypher, params, UserNode::class.java).map { user -> user.toUser() }
                }

                UserSearchParam.DISPLAY_NAME -> {
                    if (query.value.isEmpty()) throw BirdException("Query length must be greater than 0.")
                    val params = mutableMapOf<String, Any>(Pair("value", query.value))
                    val cypher =
                            """
                            MATCH (u:User)
                            WHERE u.displayName CONTAINS ${'$'}value
                            RETURN u $pagination
                        """.trimIndent()
                    return neo4j.findAll(cypher, params, UserNode::class.java).map { user -> user.toUser() }
                }

                UserSearchParam.BIO -> {
                    if (query.value.isEmpty()) throw BirdException("Query length must be greater than 0.")
                    val params = mutableMapOf<String, Any>(Pair("value", query.value))
                    val cypher =
                            """
                            MATCH (u:User)
                            WHERE u.bio CONTAINS ${'$'}value
                            RETURN u $pagination
                        """.trimIndent()
                    return neo4j.findAll(cypher, params, UserNode::class.java).map { user -> user.toUser() }
                }

                else -> throw BirdException("Invalid Query Param")
            }

            Operation.GREATER_THAN -> when (query.param) {
                UserSearchParam.CHIRP_COUNT -> {
                    val params = mutableMapOf<String, Any>(Pair("value", query.value))
                    val cypher =
                            """
                            MATCH (u:User)-[r:AUTHORED]->(:Post)
                            WITH u, count(r) as postCount
                            WHERE postCount > ${'$'}value
                            RETURN u $pagination
                        """.trimIndent()

                    return neo4j.findAll(cypher, params, UserNode::class.java).map { user -> user.toUser() }
                }

                UserSearchParam.FOLLOWER_COUNT -> {
                    val params = mutableMapOf<String, Any>(Pair("value", query.value))
                    val cypher =
                            """
                            MATCH (:User)-[r:FOLLOWING]->(u:User)
                            WITH u, count(r) as followCount
                            WHERE followCount > ${'$'}value
                            RETURN u $pagination
                        """.trimIndent()
                    return neo4j.findAll(cypher, params, UserNode::class.java).map { user -> user.toUser() }
                }

                UserSearchParam.FOLLOWING_COUNT -> {
                    val params = mutableMapOf<String, Any>(Pair("value", query.value))
                    val cypher =
                            """
                            MATCH (u:User)-[r:FOLLOWING]->(:User)
                            WITH u, count(r) as followCount
                            WHERE followCount > ${'$'}value
                            RETURN u $pagination
                        """.trimIndent()
                    return neo4j.findAll(cypher, params, UserNode::class.java).map { user -> user.toUser() }
                }

                else -> throw BirdException("Invalid Query Param")
            }

            Operation.LESS_THAN -> when (query.param) {
                UserSearchParam.CHIRP_COUNT -> {
                    val params = mutableMapOf<String, Any>(Pair("value", query.value))
                    val cypher =
                            """
                            MATCH (u:User)-[r:AUTHORED]->(:Post)
                            WITH u, count(r) as postCount
                            WHERE postCount < ${'$'}value
                            RETURN u $pagination
                        """.trimIndent()
                    return neo4j.findAll(cypher, params, UserNode::class.java).map { user -> user.toUser() }
                }

                UserSearchParam.FOLLOWER_COUNT -> {
                    val params = mutableMapOf<String, Any>(Pair("value", query.value))
                    val cypher =
                            """
                            MATCH (:User)-[r:FOLLOWING]->(u:User)
                            WITH u, count(r) as followCount
                            WHERE followCount < ${'$'}value
                            RETURN u $pagination
                        """.trimIndent()
                    return neo4j.findAll(cypher, params, UserNode::class.java).map { user -> user.toUser() }
                }

                UserSearchParam.FOLLOWING_COUNT -> {
                    val params = mutableMapOf<String, Any>(Pair("value", query.value))
                    val cypher =
                            """
                            MATCH (u:User)-[r:FOLLOWING]->(:User)
                            WITH u, count(r) as followCount
                            WHERE followCount < ${'$'}value
                            RETURN u $pagination
                        """.trimIndent()
                    return neo4j.findAll(cypher, params, UserNode::class.java).map { user -> user.toUser() }
                }

                else -> throw BirdException("Invalid Query Param")
            }

            else -> throw BirdException("Invalid Query Operation")
        }
    }
}