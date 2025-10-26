package target.infra.auth

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.slf4j.LoggerFactory
import target.infra.http.dto.ErrorResponse

private val log = LoggerFactory.getLogger("loginRoute")

// MVP: Hardcoded credentials
var mvpUsername = "admin"
var mvpPassword = "admin123"
private val defaultRoles = listOf("ADMIN")

@Serializable
data class LoginRequest(
  val username: String,
  val password: String
)

@Serializable
data class LoginResponse(
  val token: String
)

@Suppress("TooGenericExceptionCaught")
fun loginRoute(jwtService: JwtService): RoutingHttpHandler {
  return routes(
    "/login" bind Method.POST to { request ->
      try {
        val body = request.bodyString()
        val loginRequest = Json.decodeFromString(LoginRequest.serializer(), body)

        if (loginRequest.username == mvpUsername && loginRequest.password == mvpPassword) {
          val token = jwtService.createToken(
            userId = loginRequest.username,
            roles = defaultRoles
          )
          val response = LoginResponse(token = token)

          log.info("Successful login for user: ${loginRequest.username}")

          Response(Status.OK)
            .header("Content-Type", "application/json")
            .body(Json.encodeToString(LoginResponse.serializer(), response))
        } else {
          log.warn("Failed login attempt for user: ${loginRequest.username}")

          val errorResponse = ErrorResponse(error = "Invalid username or password")
          Response(Status.UNAUTHORIZED)
            .header("Content-Type", "application/json")
            .body(Json.encodeToString(ErrorResponse.serializer(), errorResponse))
        }
      } catch (e: Exception) {
        log.error("Error processing login request: ${e.message}")

        val errorResponse = ErrorResponse(error = "Invalid request format")
        Response(Status.BAD_REQUEST)
          .header("Content-Type", "application/json")
          .body(Json.encodeToString(ErrorResponse.serializer(), errorResponse))
      }
    }
  )
}
