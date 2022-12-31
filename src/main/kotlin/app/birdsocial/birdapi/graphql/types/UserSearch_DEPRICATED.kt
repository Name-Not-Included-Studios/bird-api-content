package app.birdsocial.birdapi.graphql.types

data class UserSearch_DEPRICATED(
    val usernameEquals: String?,
    val usernameContains: String?,
    val displayName: String?,
    val bio: String?,
    val isVerified: Boolean?,
    val chirpCount: Int?,
    val followersCount: Int?,
    val followingCount: Int?,
)
