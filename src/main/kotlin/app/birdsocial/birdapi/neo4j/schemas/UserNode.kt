package app.birdsocial.birdapi.neo4j.schemas

import app.birdsocial.birdapi.graphql.types.User
import org.neo4j.ogm.annotation.GeneratedValue
import org.neo4j.ogm.annotation.Id
import java.util.*
import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Property
import org.neo4j.ogm.annotation.Relationship
import java.time.LocalDateTime

@NodeEntity(label = "User")
//data class UserNode (
//        var userId: String,
//        var email: String,
//        var password: String,
//        var refreshToken: String,
//        var creationDate: LocalDateTime = LocalDateTime.now(),
//) {
//        @Property var username: String? = null
//        @Property var displayName: String? = null
//        @Property var lastLogin: LocalDateTime? = null
//        @Property var bio: String? = null
//        @Property var websiteUrl: String? = null
//        @Property var avatarUrl: String? = null
//        @Property var chirpCount: Int? = null
//        @Property var followersCount: Int? = null
//        @Property var followingCount: Int? = null

data class UserNode (
        var userId: String = "00000000-0000-0000-0000-000000000000",
        var email: String = "",
        var username: String = "",
        var displayName: String = "",
        var password: String = "",
        var refreshToken: String = "",
        var lastLogin: LocalDateTime = LocalDateTime.now(),
        var creationDate: LocalDateTime = LocalDateTime.now(),
        var bio: String = "",
        var websiteUrl: String = "",
        var avatarUrl: String = "",
) {
        @Id @GeneratedValue
        var id: Long? = null

        @Relationship("FOLLOWING", direction = Relationship.Direction.OUTGOING)
        var following: MutableList<UserNode> = mutableListOf()

        @Relationship(type = "FOLLOWING", direction = Relationship.Direction.INCOMING)
        var followedBy: MutableList<UserNode> = mutableListOf()

        @Relationship("AUTHORED")
        var posts: MutableList<PostNode> = mutableListOf()

        @Relationship("LIKED", direction = Relationship.Direction.OUTGOING)
        var liked: MutableList<PostNode> = mutableListOf()

//        @Relationship("HAS_PERMISSION", direction = Relationship.Direction.OUTGOING)
//        var hasPermission: MutableList<PermissionNode> = mutableListOf()

        fun toUser(): User {
                return User(
                        userId,
                        username,
                        displayName,
                        bio,
                        websiteUrl,
                        avatarUrl,
                        posts.size,
                        followedBy.size,
                        following.size
                )
        }
}
