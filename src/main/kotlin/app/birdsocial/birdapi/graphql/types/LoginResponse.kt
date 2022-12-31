package app.birdsocial.birdapi.graphql.types

data class LoginResponse (
    val user: User,
    val access_token: String,
    val refresh_token: String,
)
