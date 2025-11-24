package target.app.asset.uc

import target.app.asset.command.UpsertDatapointCommand
import target.app.asset.port.out.AssetPersistPort

class UpsertAssetDatapoint(private val assetPersistPort: AssetPersistPort){

    fun updateOne(command: UpsertDatapointCommand) {
        assetPersistPort.upsertAssetStatus(command.assetTitle, command.assetDatapoint)
    }

    fun updateAssetList(commandList: List<UpsertDatapointCommand>) = commandList.forEach { updateOne(it) }

}
