package app.birdsocial.birdapi.neo4j.schemas

import org.neo4j.ogm.annotation.NodeEntity
import java.util.*

data class UserGQL (
    var userId: UUID,
    var username: String = "default",
    var displayName: String = "Default",
    var bio: String = "",
    var websiteUrl: String = "",
    var avatarUrl: String = "",
    var isVerified: Boolean = false,
    var chirpCount: Int = 0,
    var followersCount: Int = 0,
    var followingCount: Int = 0,
)

@NodeEntity(label = "User")
data class UserN4J (
    var userId: String = "00000000-0000-0000-0000-000000000000",
    var username: String = "default",
    var displayName: String = "Default",
    var bio: String = "",
    var websiteUrl: String = "",
    var avatarUrl: String = "",
    var isVerified: Boolean = false,
    var chirpCount: Int = 0,
    var followersCount: Int = 0,
    var followingCount: Int = 0,
) {
    fun toUserGQL() : UserGQL {
        return UserGQL(
            UUID.fromString(userId),
            username,
            displayName,
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
