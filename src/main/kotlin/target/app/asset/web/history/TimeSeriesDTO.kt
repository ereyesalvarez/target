package target.app.asset.web.history

import kotlinx.serialization.Serializable

@Serializable
data class TimeSeriesDTO(
  val date: String,
  val balance: Double,
  val contribution: Double? = null,
  val gain: Double? = null
)
