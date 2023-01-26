package app.birdsocial.birdapi.repository

import app.birdsocial.birdapi.helper.SentryHelper
import app.birdsocial.birdapi.neo4j.schemas.PostNode
import app.birdsocial.birdapi.neo4j.schemas.UserNode
import org.neo4j.cypherdsl.core.Cypher
import org.springframework.data.neo4j.repository.Neo4jRepository
import org.springframework.data.neo4j.repository.query.Query
import org.springframework.data.repository.query.Param

interface UserRepository : Neo4jRepository<UserNode, String> {
//    @Query("MATCH (n:User) WHERE n.paramName = \$param RETURN n")
//    fun findAllByParam(@Param("paramName") paramName: String, @Param("param") param: String): List<UserNode>
}