package app.birdsocial.birdapi.graphql.types

data class Pagination(
    val page: Int,
    val pageSize: Int,
) {
    override fun toString(): String {
        return "SKIP ${page * pageSize} LIMIT $pageSize"
    }
}
