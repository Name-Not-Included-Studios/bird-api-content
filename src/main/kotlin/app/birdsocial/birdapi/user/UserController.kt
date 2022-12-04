package app.birdsocial.birdapi

import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
//import org.springframework.web.context.request.RequestAttributes
//import org.springframework.web.context.request.RequestContextHolder

//import org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST

//TODO Fix everything
@Controller
class UserController {

//    private UserDao userDao;

    // @QueryMapping
    // public recentPosts(@Argument count: Int, @Argument offset: Int): List<Post> {
    //     return userDao.getRecentPosts(count, offset);

    @QueryMapping
	fun greeting(): String {
		return "Hello"
//		val attributes: RequestAttributes? = RequestContextHolder.getRequestAttributes();
//		return "Hello " + attributes.getAttribute(RequestAttributeFilter.NAME_ATTRIBUTE, SCOPE_REQUEST);
	}
}