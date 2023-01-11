package app.birdsocial.birdapi.helper

import app.birdsocial.birdapi.exceptions.AuthException
import app.birdsocial.birdapi.exceptions.BirdException
import app.birdsocial.birdapi.exceptions.ThrottleRequestException
import io.sentry.Sentry
import io.sentry.SpanStatus
import org.springframework.stereotype.Component

@Component
class SentryHelper {
    final inline fun <T> captureExceptionsOnly(body: () -> T): T =
        try {
            body()
        } catch (ex: Exception) {
            Sentry.captureException(ex)
            throw ex
        }

    // If there is an active span/transaction, create a childSpan and catch exceptions. If there is not any active span/transaction then it will start a transaction
    final inline fun <T> span(op: String, desc: String, body: () -> T): T {
//        val span = if (Sentry.getSpan() != null) Sentry.getSpan().startChild(op, desc) else Sentry.startTransaction(op, desc, true)
        val span = Sentry.getSpan()?.startChild(op, desc) ?: Sentry.startTransaction(op, desc, true)

        try {
            span.status = SpanStatus.OK
            return body()
        } catch (ex: Exception) {
            span.throwable = ex
            span.status = when (ex) {
                is ThrottleRequestException -> SpanStatus.UNAVAILABLE
                is AuthException -> SpanStatus.UNAUTHENTICATED
                is BirdException -> SpanStatus.INTERNAL_ERROR
                else -> SpanStatus.UNKNOWN_ERROR
            }
            throw ex
        } finally {
            span.finish();
        }
    }
}