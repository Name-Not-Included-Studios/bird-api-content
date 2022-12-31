package app.birdsocial.birdapi.graphql.types

import java.util.*

data class PostInput (
    val userId: UUID,
    val content: String,
    val annotation: String?,
    val parentId: UUID?,
)
