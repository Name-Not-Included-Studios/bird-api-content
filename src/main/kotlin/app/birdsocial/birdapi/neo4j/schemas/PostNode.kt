package app.birdsocial.birdapi.neo4j.schemas

import org.neo4j.ogm.annotation.GeneratedValue
import org.neo4j.ogm.annotation.Id
import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Relationship

@NodeEntity(label = "Post")
data class PostNode (
    var postId: String = "00000000-0000-0000-0000-000000000000",
    var title: String = "Post Title",
    var body: String = "The post body can be much longer",
    var likeCount: Int = 0
) {
    @Id @GeneratedValue
    var id: Long? = null

    @Relationship(type = "AUTHORED", direction = Relationship.Direction.INCOMING)
    var authoredBy: MutableList<UserNode> = mutableListOf()
}