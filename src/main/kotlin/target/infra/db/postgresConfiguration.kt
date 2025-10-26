package target.infra.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.micrometer.core.instrument.MeterRegistry
import org.jdbi.v3.core.Jdbi
import org.slf4j.LoggerFactory
import target.infra.properties.definition.AppDBConfig

private val log = LoggerFactory.getLogger("postgresConfig")

fun getJdbiInstance(appDBConfig: AppDBConfig, registry: MeterRegistry? = null): Jdbi {
  return Jdbi.create(getDatasource(appDBConfig, registry))
}

fun getDatasource(appDBConfig: AppDBConfig, registry: MeterRegistry? = null): HikariDataSource {
  log.info("Application DB configuration: $appDBConfig")
  val hikariConfig = obtainHikariDSConfig(appDBConfig, registry)
  val ds = HikariDataSource(hikariConfig)
  return ds
}

@Suppress("MagicNumber")
private fun obtainHikariDSConfig(appDBConfig: AppDBConfig, registry: MeterRegistry? = null): HikariConfig {

  val config = HikariConfig()
  config.jdbcUrl = appDBConfig.jdbc
  config.username = appDBConfig.username
  config.password = appDBConfig.password
  // Connection pool sizing for single user and memory constraints
  config.maximumPoolSize = 2  // Very small pool since only one user
  config.minimumIdle = 1      // Keep one connection alive
  // Connection lifecycle - longer intervals for memory efficiency
  config.idleTimeout = 300000        // 5 minutes - longer idle time
  config.maxLifetime = 900000        // 15 minutes - rotate connections occasionally
  config.leakDetectionThreshold = 30000  // 30 seconds - catch connection leaks

  // Supabase-specific optimizations
  config.addDataSourceProperty("cachePrepStmts", "true")
  config.addDataSourceProperty("prepStmtCacheSize", "50")     // Reduced from 250
  config.addDataSourceProperty("prepStmtCacheSqlLimit", "1024") // Reduced from 2048
  config.addDataSourceProperty("useServerPrepStmts", "true")  // Let Supabase handle prep statements
  config.addDataSourceProperty("rewriteBatchedStatements", "true") // Optimize batch operations

  // Connection validation
  config.validationTimeout = 3000
  config.connectionTestQuery = "SELECT 1"

  if (registry != null) {
    config.metricRegistry = registry
  }
  return config
}
