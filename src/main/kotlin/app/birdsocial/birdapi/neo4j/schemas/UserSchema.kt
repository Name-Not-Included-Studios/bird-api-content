package app.birdsocial.birdapi.neo4j.schemas

import org.neo4j.ogm.annotation.NodeEntity
import java.util.*

//@NodeEntity(label = "User")
//@NodeEntity()
//data class User (
//    val userId: UUID,
//    val username: String,
//    val displayName: String,
//    val bio: String,
//    val websiteUrl: String,
//    val avatarUrl: String,
//    val isVerified: Boolean,
//    val chirpCount: Int,
//    val followersCount: Int,
//    val followingCount: Int,
//) : Entity()

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
            UUID.fromString(userId)
        )
    }
}

//@NodeEntity()
//class User (
//    _userId: UUID,
//    _username: String,
//    _displayName: String,
//    _bio: String,
//    _websiteUrl: String,
//    _avatarUrl: String,
//    _isVerified: Boolean,
//    _chirpCount: Int,
//    _followersCount: Int,
//    _followingCount: Int,
//) {
//    val userId: UUID = _userId
//    val username: String = _username
//    val displayName: String = _displayName
//    val bio: String = _bio
//    val websiteUrl: String = _websiteUrl
//    val avatarUrl: String = _avatarUrl
//    val isVerified: Boolean = _isVerified
//    val chirpCount: Int = _chirpCount
//    val followersCount: Int = _followersCount
//    val followingCount: Int = _followingCount
//}