package app.birdsocial.birdapi.graphql.types.user

data class UserLogin (
    val email: String,
    val username: String,
    val password: String,
)