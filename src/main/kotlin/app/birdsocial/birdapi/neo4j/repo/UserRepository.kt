package app.birdsocial.birdapi.neo4j.repo

import app.birdsocial.birdapi.helper.SentryHelper
import app.birdsocial.birdapi.neo4j.schemas.UserNode
import org.neo4j.cypherdsl.core.Cypher
import org.springframework.data.neo4j.repository.Neo4jRepository

interface UserRepository : Neo4jRepository<UserNode, String>