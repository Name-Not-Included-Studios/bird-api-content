package app.birdsocial.birdapi.neo4j.repo

import app.birdsocial.birdapi.neo4j.schemas.PostNode
import org.springframework.data.neo4j.repository.Neo4jRepository

interface PostRepository : Neo4jRepository<PostNode, String> {}