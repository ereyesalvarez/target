package target.app.asset.port.out

import target.app.asset.model.AssetCategory
import target.app.asset.model.AssetType
import java.util.Currency

data class AssetPersistItem(
  val id: Int,
  val title: String,
  val type: AssetType,
  val categoryCode: AssetCategory,
  val currency: Currency,
  val archived: Boolean = false,
  val archivedAt: String? = null
)
