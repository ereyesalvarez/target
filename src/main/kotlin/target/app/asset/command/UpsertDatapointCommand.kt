package target.app.asset.command

import target.app.asset.model.AssetDataPoint

data class UpsertDatapointCommand(
    val assetTitle: String,
    val assetDatapoint: AssetDataPoint
)
