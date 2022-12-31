package app.birdsocial.birdapi.graphql.types

data class UserSearchCriteria (
    val usernameContains: String?,
    val usernameStartsWith: String?,
    val usernameEndsWith: String?,
    val followerCountGreaterThan: Int?,
    val followerCountLessThan: Int?,
    val bioContains: String?,
    val usernameFuzzySearch: String?,
)