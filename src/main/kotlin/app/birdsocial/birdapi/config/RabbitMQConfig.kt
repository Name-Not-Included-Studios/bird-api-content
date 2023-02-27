package app.birdsocial.birdapi.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.URL

@Configuration
class RabbitMQConfigProvider {
    @Bean
    fun getRabbitMQConfig(): RabbitMQConfig {
//        return RabbitMQConfig()
        return RabbitMQConfig(
            "172.17.0.2",
            5672u,
            "user",
            "pass"
        )
    }
}

data class RabbitMQConfig (
    val url: String,
    val port: UShort = 5672u,
    val username: String,
    val password: String,
)