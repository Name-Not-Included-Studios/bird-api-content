package app.birdsocial.birdapi.neo4j.schemas

import app.birdsocial.birdapi.graphql.types.User
import org.neo4j.ogm.annotation.GeneratedValue
import org.neo4j.ogm.annotation.Id
import java.util.*
import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Relationship
import java.time.LocalDateTime

@NodeEntity(label = "User")
data class UserNode (
        var userId: String = "00000000-0000-0000-0000-000000000000",
        var email: String = "email@example.com",
        var username: String = "default",
        var displayName: String = "Default",
        var password: String = "",
        var refreshToken: String = "",
        var lastLogin: LocalDateTime = LocalDateTime.now(),
        var creationDate: LocalDateTime = LocalDateTime.now(),
        var bio: String = "",
        var websiteUrl: String = "",
        var avatarUrl: String = "",
        var chirpCount: Int = 0,
        var followersCount: Int = 0,
        var followingCount: Int = 0,
) {
        @Id @GeneratedValue
        var id: Long? = null

        @Relationship("FOLLOWING", direction = Relationship.Direction.OUTGOING)
        var following: MutableList<UserNode> = mutableListOf()

        @Relationship(type = "FOLLOWING", direction = Relationship.Direction.INCOMING)
        var followedBy: MutableList<UserNode> = mutableListOf()

        @Relationship("AUTHORED")
        var authored: MutableList<PostNode> = mutableListOf()

        fun toUser(): User {
                return User(
                        UUID.fromString(userId),
                        email,
                        username,
                        displayName,
                        password,
                        bio,
                        websiteUrl,
                        avatarUrl,
                        chirpCount,
                        followersCount,
                        followingCount
                )
        }
}
