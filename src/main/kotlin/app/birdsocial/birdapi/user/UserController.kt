package app.birdsocial.birdapi

@Controller
class UserController {

    private UserDao userDao;

    @QueryMapping
    public recentPosts(@Argument count: Int, @Argument offset: Int): List<Post> {
        return userDao.getRecentPosts(count, offset);
    }
}