package target.app.asset.domain.uc

import target.app.asset.domain.command.UpsertDatapointCommand
import target.app.asset.domain.port.out.AssetPersistPort

class UpsertAssetDatapoint(private val assetPersistPort: AssetPersistPort){

    fun updateOne(command: UpsertDatapointCommand) {
        assetPersistPort.upsertAssetStatus(command.assetTitle, command.assetDatapoint)
    }

    fun updateAssetList(commandList: List<UpsertDatapointCommand>) = commandList.forEach { updateOne(it) }

}
