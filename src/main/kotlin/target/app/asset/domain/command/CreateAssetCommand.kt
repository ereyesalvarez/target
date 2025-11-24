package target.app.asset.domain.command

import target.app.asset.domain.model.AssetCategory
import target.app.asset.domain.model.AssetType
import java.util.Currency

data class CreateAssetCommand(
  val title: String,
  val type: AssetType,
  val category: AssetCategory,
  val currency: Currency
  )
