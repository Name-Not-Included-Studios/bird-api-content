package app.birdsocial.birdapi.exceptions

import graphql.ErrorType
import graphql.GraphQLError
import graphql.GraphqlErrorBuilder
import graphql.schema.DataFetchingEnvironment
import io.sentry.Sentry
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter
import org.springframework.stereotype.Component

class BirdException(message: String?) : RuntimeException(message)
class AuthException() : RuntimeException()
class ThrottleRequestException() : RuntimeException()

@Component
class ExceptionHandler : DataFetcherExceptionResolverAdapter() {
    override fun resolveToSingleError(ex: Throwable, env: DataFetchingEnvironment): GraphQLError? {
        return when (ex) {
            is ThrottleRequestException -> {
                GraphqlErrorBuilder.newError()
                    .errorType(ErrorType.ExecutionAborted)
                    .message("You are sending too many requests, please wait and try again.")
                    .path(env.executionStepInfo.path)
                    .location(env.field.sourceLocation)
                    .build()
            }

            is AuthException -> {
                GraphqlErrorBuilder.newError()
                    .errorType(ErrorType.ValidationError)
                    .message("Auth Failed")
                    .path(env.executionStepInfo.path)
                    .location(env.field.sourceLocation)
                    .build()
            }

            is BirdException -> {
                Sentry.captureException(ex)

                GraphqlErrorBuilder.newError()
                    .errorType(ErrorType.OperationNotSupported)
                    .message(ex.message)
                    .path(env.executionStepInfo.path)
                    .location(env.field.sourceLocation)
                    .build()
            }

            else -> {
                null
            }
        }
    }
}
