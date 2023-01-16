package app.birdsocial.birdapi.neo4j.schemas

import app.birdsocial.birdapi.graphql.types.Post
import org.springframework.data.neo4j.core.schema.GeneratedValue
import org.springframework.data.neo4j.core.schema.Id
import org.springframework.data.neo4j.core.schema.Node
import org.springframework.data.neo4j.core.schema.Property
import org.springframework.data.neo4j.core.schema.Relationship
import org.springframework.data.neo4j.core.support.UUIDStringGenerator
import java.time.LocalDateTime
import java.util.*

@Node("Post")
data class PostNode(
    @Id @GeneratedValue(UUIDStringGenerator::class) val id: String,
//    var content: List<String> = listOf(), // TODO - Change to content
    @Property val content: String,
    @Property var annotation: String,
    @Property var annotationDate: LocalDateTime,
    @Property var creationDate: LocalDateTime,
) {
    @Relationship(type = "AUTHORED", direction = Relationship.Direction.INCOMING)
    var authors: MutableList<UserNode> = mutableListOf()
    @Relationship(type = "SUB_POST", direction = Relationship.Direction.INCOMING)
    var parentPost: PostNode? = null
    @Relationship(type = "SUB_POST", direction = Relationship.Direction.OUTGOING)
    var childPosts: MutableList<PostNode> = mutableListOf()
    @Relationship(type = "LIKED", direction = Relationship.Direction.INCOMING)
    var likedBy: MutableList<UserNode> = mutableListOf()

    fun toPost(): Post {
        return Post(
            id,
//            authoredBy?.id ?: throw BirdException("No Author"),
            authors.first().id,
            content,
            likedBy.size,
            true,
            annotation,
            parentPost?.id,
        )
    }

    fun save(): Boolean {
        TODO("Not yet implemented")
    }

    fun delete(): Boolean {
        TODO("Not yet implemented")
    }
}