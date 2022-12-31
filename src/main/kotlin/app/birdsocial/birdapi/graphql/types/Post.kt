package app.birdsocial.birdapi.graphql.types

import java.util.*

data class Post (
    val postId: UUID,
    val userId: UUID,
    val content: String,
    val likesCount: Int,
    val isPublished: Boolean,
    val annotation: String?,
    val parentId: UUID?,
)