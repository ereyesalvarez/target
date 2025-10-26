package target.infra.http.routes

import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes

fun appUtilsRoutes(meterRegistry: PrometheusMeterRegistry): RoutingHttpHandler {
  return routes(
    healthHandler,
    metricRoutes(meterRegistry)
  )
}

val healthHandler = routes(
  "/ping" bind Method.GET to { Response(Status.OK).body("pong") }
)

private fun metricRoutes(meterRegistry: PrometheusMeterRegistry) = routes(
  "/metrics" bind Method.GET to {
    Response(Status.OK).body(meterRegistry.scrape())
  }
)
