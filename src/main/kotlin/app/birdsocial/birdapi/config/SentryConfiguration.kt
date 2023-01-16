package app.birdsocial.birdapi.config

import io.sentry.Sentry
import io.sentry.SentryOptions
import jakarta.annotation.PostConstruct
import org.springframework.core.env.Environment
import org.springframework.core.env.get
import org.springframework.stereotype.Component

@Component
class SentryConfiguration(val environment: Environment) {
    @PostConstruct
    fun getSentryOptions() {
        val options = SentryOptions()
        options.dsn = environment["SENTRY_DSN"] ?: ""
        // Set tracesSampleRate to 1.0 to capture 100% of transactions for performance monitoring.
        // We recommend adjusting this value in production.
        options.tracesSampleRate = 1.0
        // When first trying Sentry it's good to see what the SDK is doing:
        options.isDebug = false
        options.environment = environment["ENVIRONMENT"] ?: "unknown"

        Sentry.init(options)
    }
}