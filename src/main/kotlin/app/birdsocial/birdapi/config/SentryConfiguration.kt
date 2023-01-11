package app.birdsocial.birdapi.config

import io.sentry.Sentry
import io.sentry.SentryOptions
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component

@Component
class SentryConfiguration(val envData: EnvironmentData) {
    @PostConstruct
    fun getSentryOptions() {
        val options = SentryOptions()
        options.dsn = "https://2dce25466378481d83e3762fcd44f17e@o4504471975297024.ingest.sentry.io/4504471977197568"
        // Set tracesSampleRate to 1.0 to capture 100% of transactions for performance monitoring.
        // We recommend adjusting this value in production.
        options.tracesSampleRate = 1.0
        // When first trying Sentry it's good to see what the SDK is doing:
        options.isDebug = true
        options.environment = envData.getData("ENVIRONMENT")

        Sentry.init(options)

//        return SentryOptions().also { options ->
//            options.dsn = "https://2dce25466378481d83e3762fcd44f17e@o4504471975297024.ingest.sentry.io/4504471977197568"
//            // Set tracesSampleRate to 1.0 to capture 100% of transactions for performance monitoring.
//            // We recommend adjusting this value in production.
//            options.tracesSampleRate = 1.0
//            // When first trying Sentry it's good to see what the SDK is doing:
//            options.isDebug = true
//            options.environment = envData.getData("ENVIRONMENT")
//        }
    }
}