package target.app.asset.command

import target.app.asset.model.AssetCategory
import target.app.asset.model.AssetType
import java.util.Currency

data class CreateAssetCommand(
  val title: String,
  val type: AssetType,
  val category: AssetCategory,
  val currency: Currency
  )
