package app.birdsocial.birdapi.neo4j.repo

import app.birdsocial.birdapi.helper.SentryHelper
import app.birdsocial.birdapi.neo4j.schemas.PostNode
import app.birdsocial.birdapi.neo4j.schemas.UserNode
import org.neo4j.cypherdsl.core.Cypher
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.neo4j.repository.Neo4jRepository

interface PostRepository : Neo4jRepository<PostNode, String>