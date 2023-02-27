package app.birdsocial.birdapi.repository

import app.birdsocial.birdapi.neo4j.schemas.UserNode
import org.springframework.data.neo4j.repository.Neo4jRepository

interface UserRepository : Neo4jRepository<UserNode, String> {
//    @Query("MATCH (n:User) WHERE n.paramName = \$param RETURN n")
//    fun findAllByParam(@Param("paramName") paramName: String, @Param("param") param: String): List<UserNode>
}