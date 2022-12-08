package app.birdsocial.birdapi.graphql.schemas.user

data class UserSearch(
    val username: String?,
    val displayName: String?,
    val bio: String?,
    val isVerified: Boolean?,
    val chirpCount: Int?,
    val followersCount: Int?,
    val followingCount: Int?,
)
