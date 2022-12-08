package app.birdsocial.birdapi.user

import app.birdsocial.birdapi.user.User
import app.birdsocial.birdapi.user.UserArgs
import app.birdsocial.birdapi.user.UserDao
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class UserResolver(_userDao: UserDao) {
	private val userDao: UserDao = _userDao

	// This is the function that gets data from neo4j
	//@SchemaMapping
	@QueryMapping
	fun users(@Argument userArgs: UserArgs): List<User> {
		val users = userDao.getUsers().filter {
			user ->
			!((userArgs.username != null && userArgs.username != user.username) ||
					(userArgs.displayName != null && userArgs.displayName != user.displayName) ||
					(userArgs.bio != null && userArgs.bio != user.bio) ||
					(userArgs.isVerified != null && userArgs.isVerified != user.isVerified)||
					(userArgs.chirpCount != null && userArgs.chirpCount != user.chirpCount) ||
					(userArgs.followersCount != null && userArgs.followersCount != user.followersCount) ||
					(userArgs.followingCount != null && userArgs.followingCount != user.followingCount))
		}
		return users
	}
}