package target.app.asset.model

import java.util.Currency

data class AssetWithData (
  val title: String,
  val type: AssetType,
  val category: AssetCategory,
  val currency: Currency,
  val items: List<AssetDataPoint>
)
