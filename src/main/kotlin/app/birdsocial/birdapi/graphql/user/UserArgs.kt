package app.birdsocial.birdapi.graphql.user

data class UserArgs(
                var username: String?,
                var displayName: String?,
                var bio: String?,
                var isVerified: Boolean?,
                var chirpCount: Int?,
                var followersCount: Int?,
                var followingCount: Int?,
)
