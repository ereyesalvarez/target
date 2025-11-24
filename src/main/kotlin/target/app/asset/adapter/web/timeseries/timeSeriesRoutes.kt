package target.app.asset.adapter.web.timeseries

import target.app.asset.adapter.web.history.AssetTimeSeriesDTO
import target.app.asset.adapter.web.history.TimeSeriesDTO
import org.http4k.core.Method.GET
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.format.KotlinxSerialization
import org.http4k.routing.bind
import org.http4k.routing.routes
import target.app.asset.model.AssetWithData
import target.app.asset.uc.TimeSeriesUC
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


private val json = KotlinxSerialization
private val responseLens = json.autoBody<List<AssetTimeSeriesDTO>>().toLens()

fun timeSeriesRoutes(timeSeriesUC: TimeSeriesUC) = routes(
  "/v1/assets/timeseries/daily" bind GET to { _ ->
    val startDate = LocalDateTime.now().minusMonths(1)
    val assets = timeSeriesUC.dailyTimeSeries(startDate)
    val dtos = assets.map { it.toAssetTimeSeriesDTO("yyyy-MM-dd") }
    Response(OK).with(responseLens of dtos)
  },

  "/v1/assets/timeseries/monthly" bind GET to { _ ->
    val assets = timeSeriesUC.monthlyTimeSeries()
    val dtos = assets.map { it.toAssetTimeSeriesDTO("yyyy-MM") }
    Response(OK).with(responseLens of dtos)
  },

  "/v1/assets/timeseries/yearly" bind GET to { _ ->
    val assets = timeSeriesUC.yearlyTimeSeries()
    val dtos = assets.map { it.toAssetTimeSeriesDTO("yyyy") }
    Response(OK).with(responseLens of dtos)
  }
)

// --- helpers ---

private fun AssetWithData.toAssetTimeSeriesDTO(dateFormat: String): AssetTimeSeriesDTO {
  val formatter = DateTimeFormatter.ofPattern(dateFormat)

  return AssetTimeSeriesDTO(
    name = this.title,
    type = this.type.name,
    typeGroup = this.type.typeGroup, // Not clear what this should be, leaving empty for now
    category = this.category.name,
    currency = this.currency,
    items = this.items.map { dataPoint ->
      TimeSeriesDTO(
        date = dataPoint.date.format(formatter), // Placeholder - need actual date from datapoint
        balance = dataPoint.balance.toDouble(),
        contribution = dataPoint.contribution?.toDouble(),
        gain = dataPoint.gain?.toDouble()
      )
    }
  )
}

