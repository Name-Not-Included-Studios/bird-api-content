package app.birdsocial.birdapi

import app.birdsocial.birdapi.user.User
import app.birdsocial.birdapi.user.UserArgs
import app.birdsocial.birdapi.user.UserDao
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

//TODO Fix everything
@Controller
class UserResolver(_userDao: UserDao) {
	private val userDao: UserDao = _userDao

	// This is the function that gets data from neo4j
	//@SchemaMapping
	@QueryMapping
	fun users(): List<User> {
		return userDao.getUsers()
	}



//	@QueryMapping
//	fun getUserOld(username: String, displayName: String): User {
//		println("ID: $username")
//		return User(
//			userId = UUID.randomUUID(),
//			username = username,
//			displayName = displayName,
//			bio = "Bio",
//			websiteUrl = "website",
//			avatarUrl = "avatar",
//			isVerified = false,
//			chirpCount = 42069,
//			followersCount = 69,
//			followingCount = 420
//		)
//	}

//    private UserDao userDao;

    // @QueryMapping
    // public recentPosts(@Argument count: Int, @Argument offset: Int): List<Post> {
    //     return userDao.getRecentPosts(count, offset);

    @QueryMapping
	fun getApiVersion(): String {
		return "0.1-SNAPSHOT"
//		val attributes: RequestAttributes? = RequestContextHolder.getRequestAttributes();
//		return "Hello " + attributes.getAttribute(RequestAttributeFilter.NAME_ATTRIBUTE, SCOPE_REQUEST);
	}
}