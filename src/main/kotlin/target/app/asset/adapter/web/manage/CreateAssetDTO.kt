package target.app.asset.adapter.web.manage

import kotlinx.serialization.Serializable
import target.app.asset.model.AssetCategory
import target.app.asset.model.AssetType

@Serializable
data class CreateAssetDTO(
  val title: String,
  val type: AssetType,
  val category: AssetCategory,
  val currency: String
)
