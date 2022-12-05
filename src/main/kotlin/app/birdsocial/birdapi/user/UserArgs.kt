package app.birdsocial.birdapi.user

data class UserArgs (
    val username: String,
    val displayName: String,
    val bio: String,
    val websiteUrl: String,
    val avatarUrl: String,
    val isVerified: Boolean,
    val chirpCount: Int,
    val followersCount: Int,
    val followingCount: Int,
)