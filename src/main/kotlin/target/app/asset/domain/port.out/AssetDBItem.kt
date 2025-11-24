package target.app.asset.domain.port.out

import target.app.asset.domain.model.AssetCategory
import target.app.asset.domain.model.AssetType
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
