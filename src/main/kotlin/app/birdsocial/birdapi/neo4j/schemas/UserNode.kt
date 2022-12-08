package app.birdsocial.birdapi.neo4j.schemas

import app.birdsocial.birdapi.graphql.types.user.User
import org.neo4j.ogm.annotation.GeneratedValue
import org.neo4j.ogm.annotation.Id
import java.util.*
import org.neo4j.ogm.annotation.NodeEntity

//data class UserGQL(
//        var userId: UUID,
//        var username: String = "defaultusername",
//        var displayName: String = "Default",
//        var bio: String = "",
//        var websiteUrl: String = "",
//        var avatarUrl: String = "",
//        var isVerified: Boolean = false,
//        var chirpCount: Int = 0,
//        var followersCount: Int = 0,
//        var followingCount: Int = 0,
//)

@NodeEntity(label = "User")
data class UserNode(
        var userId: String = "00000000-0000-0000-0000-000000000000",
        var email: String = "email@example.com",
        var username: String = "default",
        var displayName: String = "Default",
        var password: String = "",
        var bio: String = "",
        var websiteUrl: String = "",
        var avatarUrl: String = "",
        var isVerified: Boolean = false,
        var chirpCount: Int = 0,
        var followersCount: Int = 0,
        var followingCount: Int = 0,
) {
        @Id @GeneratedValue
        var id: Long? = null

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
                        isVerified,
                        chirpCount,
                        followersCount,
                        followingCount
                )
        }
}
