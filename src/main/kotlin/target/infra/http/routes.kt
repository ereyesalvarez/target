package target.infra.http

import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import kotlinx.serialization.json.Json
import org.http4k.core.HttpHandler
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.then
import org.http4k.filter.AllowAll
import org.http4k.filter.CorsPolicy
import org.http4k.filter.OriginPolicy
import org.http4k.filter.ServerFilters
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import target.infra.auth.JwtService
import target.infra.auth.loginRoute
import target.infra.http.dto.DefaultResponse
import target.infra.http.routes.appUtilsRoutes
import target.infra.http.routes.authFilter
import target.infra.http.routes.userIdKey


fun createRoutes(
  meterRegistry: PrometheusMeterRegistry,
  jwtService: JwtService,

  enableGlobalPermissions: Boolean = true): HttpHandler {
  val handler:HttpHandler =  exceptionHandler.then(
    routes(
      appUtilsRoutes(meterRegistry),
      loginRoute(jwtService),
      authFilter(jwtService).then(
        authenticatedUserRoutes()
      )
    )
  )
  if (enableGlobalPermissions) {
    val corsFilter = ServerFilters.Cors(
      CorsPolicy(
        originPolicy = OriginPolicy.AllowAll(),
        headers = listOf("Authorization", "Content-Type", "Accept"),
        methods = listOf(
          Method.GET, Method.POST, Method.PUT,
          Method.DELETE, Method.PATCH, Method.OPTIONS
        ),
        credentials = false
      )
    )
    return  corsFilter.then(handler)
  }
  return handler
}

private fun authenticatedUserRoutes(): RoutingHttpHandler = routes(
  "/me" bind Method.GET to { request ->
    val username = userIdKey(request)
    val payload = DefaultResponse(message = "Authenticated as $username")

    Response(Status.OK)
      .header("Content-Type", "application/json")
      .body(Json.encodeToString(DefaultResponse.serializer(), payload))
  }
)
