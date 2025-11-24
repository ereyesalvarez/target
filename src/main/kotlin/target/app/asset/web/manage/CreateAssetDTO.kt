package target.app.asset.web.manage

import kotlinx.serialization.Serializable
import target.app.asset.domain.model.AssetCategory
import target.app.asset.domain.model.AssetType

@Serializable
data class CreateAssetDTO(
  val title: String,
  val type: AssetType,
  val category: AssetCategory,
  val currency: String
)
