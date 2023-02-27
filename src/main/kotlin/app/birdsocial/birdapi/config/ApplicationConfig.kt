package app.birdsocial.birdapi.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ApplicationConfigProvider {
    @Bean
    fun getApplicationConfig(): ApplicationConfig {
        return ApplicationConfig()
    }
}

data class ApplicationConfig (
    val maxPageSize: Int = 50
)