package target.app.asset.adapter.web.history

import kotlinx.serialization.Serializable
import target.infra.serializer.CurrencyAsCodeSerializer
import java.util.*

@Serializable
data class AssetTimeSeriesDTO(
  val name: String,
  val type: String,
  val typeGroup: String,
  val category: String,
  @Serializable(with = CurrencyAsCodeSerializer::class)
  val currency: Currency,
  val items: List<TimeSeriesDTO>
)
