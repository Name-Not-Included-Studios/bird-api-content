package app.birdsocial.birdapi.graphql.user

data class UserArgs (
    var username: String?,
    var displayName: String?,
    var bio: String?,
    var isVerified: Boolean?,
    var chirpCount: Int?,
    var followersCount: Int?,
    var followingCount: Int?,
)

//data class UserArgs (
//    var username: String = "",
//    var displayName: String = "",
//    var bio: String = "",
//    var isVerified: Boolean = false,
//    var chirpCount: Int = 0,
//    var followersCount: Int = 0,
//    var followingCount: Int = 0,
//)