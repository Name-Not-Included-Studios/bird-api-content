package app.birdsocial.birdapi.neo4j.schemas

import app.birdsocial.birdapi.graphql.types.LazyUser
import app.birdsocial.birdapi.graphql.types.User
import org.springframework.data.neo4j.core.schema.*
import org.springframework.data.neo4j.core.support.UUIDStringGenerator
import java.time.Instant

//@NodeEntity(label = "User")
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

@Node("User")
data class UserNode (
        @Id @GeneratedValue(UUIDStringGenerator::class) val id: String,
        @Property var email: String,
        @Property var username: String,
        @Property var displayName: String,
        @Property var password: String,
        @Property var refreshToken: String,
        @Property var lastLogin: Instant,
        @Property var creationDate: Instant,
        @Property var bio: String,
        @Property var websiteUrl: String,
//        @Property var avatarUrl: String, // Replaced with cdn.birdsocial.app/userId/profile.png
) {
        @Relationship(type = "FOLLOWING", direction = Relationship.Direction.OUTGOING)
        var following: MutableList<UserNode> = mutableListOf()
        @Relationship(type = "FOLLOWING", direction = Relationship.Direction.INCOMING)
        var followers: MutableList<UserNode> = mutableListOf()
        @Relationship(type = "AUTHORED", direction = Relationship.Direction.OUTGOING)
        var posts: MutableList<PostNode> = mutableListOf()
        @Relationship(type = "LIKED", direction = Relationship.Direction.OUTGOING)
        var liked: MutableList<PostNode> = mutableListOf()

        fun toUser(): User {
                return User(
                        id,
                        username,
                        displayName,
                        bio,
                        websiteUrl,
                        posts.map { post -> post.toLazyPost() },
                        followers.map { user -> user.toLazyUser() },
                        following.map { user -> user.toLazyUser() },
                )
        }

        fun toLazyUser(): LazyUser {
                return LazyUser(
                        id,
                        username,
                        displayName,
                        bio,
                        websiteUrl,
                )
        }
}
