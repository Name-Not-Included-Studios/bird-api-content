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

data class PermissionNode (
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
        var chirpCount: Int = -1,
        var followersCount: Int = -1,
        var followingCount: Int = -1,
) {
        @Id @GeneratedValue
        var id: Long? = null

        @Relationship("FOLLOWING", direction = Relationship.Direction.OUTGOING)
        var following: MutableList<PermissionNode> = mutableListOf()

        @Relationship(type = "FOLLOWING", direction = Relationship.Direction.INCOMING)
        var followedBy: MutableList<PermissionNode> = mutableListOf()

        @Relationship("AUTHORED")
        var authored: MutableList<PostNode> = mutableListOf()

        @Relationship("LIKED", direction = Relationship.Direction.OUTGOING)
        var liked: MutableList<PostNode> = mutableListOf()

        @Relationship("LIKED", direction = Relationship.Direction.OUTGOING)
        var hasPermission: MutableList<PostNode> = mutableListOf()

        fun toUser(): User {
                return User(
                        userId,
                        username,
                        displayName,
                        bio,
                        websiteUrl,
                        avatarUrl,
                        chirpCount,
                        followersCount,
                        followingCount
                )
        }
}
