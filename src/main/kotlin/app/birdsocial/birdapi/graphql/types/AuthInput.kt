package app.birdsocial.birdapi.graphql.types

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class AuthInput (
    @NotBlank(message = "Email is mandatory")
    @Email(message = "Email must be valid email")
    val email: String,
    val password: String,
)