package target.app.asset.domain.command

import target.app.asset.domain.model.AssetDataPoint

data class UpsertDatapointCommand(
    val assetTitle: String,
    val assetDatapoint: AssetDataPoint
)
