package target.infra.http

import io.sentry.Sentry
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.http4k.core.Filter
import org.http4k.core.Response
import org.http4k.core.Status
import org.slf4j.LoggerFactory
import target.app.common.NotFoundException
import target.app.common.ValidationException
import target.infra.http.dto.DefaultResponse
import target.infra.http.dto.ErrorResponse

private val log = LoggerFactory.getLogger("exceptionHandler")

@Serializable
data class ValidationErrorResponse(
  val error: String,
  val field: String? = null,
  val errors: Map<String, String> = emptyMap()
)

@Suppress("TooGenericExceptionCaught")
val exceptionHandler: Filter = Filter { next ->
  { request ->
    try {
      next(request)
    } catch (e: ValidationException) {
      log.info("Validation error for ${request.uri}: ${e.message}")
      val errorResponse = ValidationErrorResponse(
        error = e.message ?: "Validation failed",
        field = e.field,
        errors = e.getAllErrors()
      )
      Response(Status.BAD_REQUEST)
        .header("Content-Type", "application/json")
        .body(Json.encodeToString(ValidationErrorResponse.serializer(), errorResponse))
    } catch (e: NotFoundException) {
      log.info("Not found Exception ${e.message}")
      val errorResponse = DefaultResponse(message = e.message ?: "Not found")
      Response(Status.NOT_FOUND)
        .header("Content-Type", "application/json")
        .body(Json.encodeToString(DefaultResponse.serializer(), errorResponse))
    } catch (e: Exception) {
      log.warn("Something went wrong in the uri ${request.uri}, ${e.message}")
      log.warn(e.stackTraceToString())
      Sentry.captureException(e)
      val errorResponse = ErrorResponse(error = e.message ?: "Something went wrong")
      Response(Status.INTERNAL_SERVER_ERROR)
        .header("Content-Type", "application/json")
        .body(Json.encodeToString(ErrorResponse.serializer(), errorResponse))
    }
  }
}
