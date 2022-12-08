package app.birdsocial.birdapi.graphql.user

import app.birdsocial.birdapi.neo4j.schemas.UserGQL
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*

@Configuration
class UserConfig {

    @Bean
    fun userDao(): UserDao {
        val users: List<UserGQL> = List<UserGQL>(
            10
        ) { i ->
            UserGQL(
                UUID.randomUUID(),
                "username$i",
                "Display Name $i",
                "Bio $i",
                "websiteUrl/$i",
                "avatarUrl/$i",
                false,
                42069 + i,
                420 + i,
                69 + i
            )
        };

        return UserDao(users);
    }
}