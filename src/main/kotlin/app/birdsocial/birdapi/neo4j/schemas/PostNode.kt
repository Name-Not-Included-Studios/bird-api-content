package app.birdsocial.birdapi.neo4j.schemas

import app.birdsocial.birdapi.graphql.types.content.Content
import app.birdsocial.birdapi.graphql.types.Post
import org.neo4j.ogm.annotation.GeneratedValue
import org.neo4j.ogm.annotation.Id
import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Relationship
import java.time.LocalDateTime

@NodeEntity(label = "Post")
data class PostNode (
    var postId: String = "00000000-0000-0000-0000-000000000000",
//    var content: List<String> = listOf(), // TODO - Change to content
    var content: String = "",
    var annotation: String = "",
    var annotationDate: LocalDateTime = LocalDateTime.now(),
    var creationDate: LocalDateTime = LocalDateTime.now(),
) {
    @Id @GeneratedValue
    var id: Long? = null

    @Relationship(type = "AUTHORED", direction = Relationship.Direction.INCOMING)
//    lateinit var authoredBy: UserNode
    var authoredBy: UserNode? = null

    @Relationship(type = "PARENTED", direction = Relationship.Direction.INCOMING)
//    lateinit var parentPost: PostNode
    var parentPost: PostNode? = null


    @Relationship(type = "PARENTED", direction = Relationship.Direction.OUTGOING)
    var childPosts: MutableList<PostNode> = mutableListOf()

    @Relationship("LIKED", direction = Relationship.Direction.INCOMING)
    var likedBy: MutableList<UserNode> = mutableListOf()

    fun toPost(): Post {
        return Post(
            postId,
            authoredBy?.userId ?: "", // TODO - make it a list of authors
            content,
            likedBy.size,
            true,
            annotation,
            parentPost?.postId,
        )
    }
}