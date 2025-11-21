package target.infra.sentry

import io.sentry.Sentry
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("SentryInit")


fun initSentry() {
  System.getenv("SENTRY_DSN")?.let { dsn ->
    Sentry.init { options ->
      options.dsn = dsn
      // Add data like request headers and IP for users,
      // see https://docs.sentry.io/platforms/java/data-management/data-collected/ for more info
      options.isSendDefaultPii = true
    }
  } ?: run {
    logger.info("Sentry DSN not found in environment variables.")
  }
}
