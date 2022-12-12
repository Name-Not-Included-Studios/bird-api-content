package app.birdsocial.birdapi.graphql.exceptions

import graphql.ErrorType
import graphql.GraphQLError
import graphql.GraphqlErrorBuilder
import graphql.schema.DataFetchingEnvironment
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter
import org.springframework.stereotype.Component

class BirdException(message: String?) : RuntimeException(message)
class AuthException() : RuntimeException()
class ThrottleRequestException(message: String?) : RuntimeException(message)

@Component
class ExceptionHandler : DataFetcherExceptionResolverAdapter() {
    override fun resolveToSingleError(ex: Throwable, env: DataFetchingEnvironment): GraphQLError? {
        return when (ex) {
            is AuthException -> {
                GraphqlErrorBuilder.newError()
                    .errorType(ErrorType.ValidationError)
                    .message("Auth Failed")
                    .path(env.executionStepInfo.path)
                    .location(env.field.sourceLocation)
                    .build()
            }

            is ThrottleRequestException -> {
                GraphqlErrorBuilder.newError()
                    .errorType(ErrorType.ExecutionAborted)
                    .message(ex.message)
                    .path(env.executionStepInfo.path)
                    .location(env.field.sourceLocation)
                    .build()
            }

            is BirdException -> {
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
