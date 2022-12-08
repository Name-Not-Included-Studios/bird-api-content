package app.birdsocial.birdapi.graphql.schemas.user

data class UserCreate (
    val username: String,
    val displayName: String,
    val password: String,
    val email: String,
)