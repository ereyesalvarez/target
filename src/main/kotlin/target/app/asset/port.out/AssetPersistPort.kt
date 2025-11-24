package target.app.asset.port.out

import target.app.asset.command.CreateAssetCommand
import target.app.asset.model.AssetDataPoint
import target.app.asset.model.AssetWithData
import java.time.LocalDate

interface AssetPersistPort {
  fun createAsset(command: CreateAssetCommand)
  fun checkIfExists(title: String): Boolean
  fun upsertAssetStatus(title: String, datapoint: AssetDataPoint)
  fun retrieveAssets(): List<AssetPersistItem>
  fun retrieveMonthlyTimeSeries(): List<AssetWithData>
  fun retrieveDailyTimeSeries(startDate: LocalDate): List<AssetWithData>
  fun retrieveYearlyTimeSeries(): List<AssetWithData>
}
