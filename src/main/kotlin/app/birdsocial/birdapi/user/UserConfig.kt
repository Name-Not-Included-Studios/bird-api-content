package app.birdsocial.birdapi.user

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*

@Configuration
class UserConfig {

    @Bean
    fun userDao(): UserDao {
        println("USER BEANS")

        val users: List<User> = List<User>(
            10
        ) { i ->
            User(
                UUID.randomUUID(),
                "username$i",
//                "Display Name $i",
//                "Bio $i",
//                "websiteUrl/$i",
//                "avatarUrl/$i",
//                false,
//                42069 + i,
//                420 + i,
//                69 + i
            )
        };

        return UserDao(users);
    }
}