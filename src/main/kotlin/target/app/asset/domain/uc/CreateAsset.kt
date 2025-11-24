package target.app.asset.domain.uc

import org.slf4j.LoggerFactory
import target.app.asset.domain.command.CreateAssetCommand
import target.app.asset.domain.port.out.AssetPersistPort

class CreateAsset(private val assetPersistPort: AssetPersistPort) {
  private val logger = LoggerFactory.getLogger(CreateAsset::class.java)


  fun execute(command: CreateAssetCommand){
    // check if an asset with this name already exists
    if(assetPersistPort.checkIfExists(command.title)){
      logger.warn("Asset with name ${command.title} already exists")
      throw Exception("Asset with name ${command.title} already exists")
    }
    assetPersistPort.createAsset(command)
  }
}
