package target

import org.http4k.core.HttpHandler
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.routes
import org.http4k.server.Http4kServer
import org.http4k.server.ServerConfig
import org.http4k.server.SunHttp
import org.http4k.server.asServer
import org.jdbi.v3.core.Jdbi
import org.slf4j.LoggerFactory
import target.app.asset.web.AssetFactory
import target.infra.auth.JwtService
import target.infra.db.getDatasource
import target.infra.http.createRoutes
import target.infra.metric.createMeterRegistry
import target.infra.properties.loader.appProps
import kotlin.random.Random

class App {
  private val log = LoggerFactory.getLogger(App::class.java)
  private val meterRegistry = createMeterRegistry()
  private val jwtService = JwtService(issuer = "target ")
  private val appConfig = appProps
  private val jdbi: Jdbi = Jdbi.create(getDatasource(appConfig.db))

  fun start(): Http4kServer {
    log.info("Starting...")
    val server = getServer()
    server.start()
    log.info("Started on port ${server.port()}")
    return server
  }

  fun getServer(): Http4kServer {
    val app = appRoutes()
    var serverConfig: ServerConfig
    if (appProps.server.port != null && appProps.server.port != 0){
      serverConfig = SunHttp(appProps.server.port)
    } else{
      val port = Random.nextInt(8000, 9000)
      serverConfig = SunHttp(port)
    }
    return app.asServer(serverConfig)
  }

  fun appRoutes(): HttpHandler {
    return createRoutes(meterRegistry, jwtService, AssetFactory(jdbi).retrieveAssetRoutes())
  }
}
