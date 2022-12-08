package app.birdsocial.birdapi.graphql.types.user

data class UserCreate (
    val email: String,
    val username: String,
    val displayName: String,
    val password: String,
)