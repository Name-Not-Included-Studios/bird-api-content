package app.birdsocial.birdapi.graphql.types

enum class UserSearchParam {
    USERNAME,
    DISPLAY_NAME,
    BIO,
    WEB_URL,
    CHIRP_COUNT,
    FOLLOWER_COUNT,
    FOLLOWING_COUNT,
}

enum class Operation {
    EQUALS,
    CONTAINS,
    GREATER_THAN,
    LESS_THAN,
}

data class UserSearch (
    val param: UserSearchParam,
    val value: String,
    val operation: Operation,
    val pagination: Pagination,
)