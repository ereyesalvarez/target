package target.app.asset.web

import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.routes
import org.jdbi.v3.core.Jdbi
import target.app.asset.adapter.out.AssetDBAdapter
import target.app.asset.domain.port.out.AssetPersistPort
import target.app.asset.domain.uc.CreateAsset
import target.app.asset.domain.uc.TimeSeriesUC
import target.app.asset.domain.uc.UpsertAssetDatapoint
import target.app.asset.web.manage.assetCreateRoutes
import target.app.asset.web.manage.assetDatapointRoutes
import target.app.asset.web.timeseries.timeSeriesRoutes

class AssetFactory(jdbi: Jdbi) {
  private val assetPersistPort: AssetPersistPort = AssetDBAdapter(jdbi)
  private val createAsset = CreateAsset(assetPersistPort)
  private val upsertAssetDatapoint = UpsertAssetDatapoint(assetPersistPort)
  private val timeSeriesUC = TimeSeriesUC(assetPersistPort)

  fun retrieveAssetRoutes(): RoutingHttpHandler {
    return routes(
    timeSeriesRoutes(timeSeriesUC),
    assetCreateRoutes(createAsset),
    assetDatapointRoutes(upsertAssetDatapoint)
    )
  }
}