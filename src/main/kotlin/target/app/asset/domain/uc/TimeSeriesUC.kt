package target.app.asset.domain.uc

import target.app.asset.domain.model.AssetWithData
import target.app.asset.domain.port.out.AssetPersistPort
import java.time.LocalDateTime

/**
 * For each asset in the db we return the data.
 * It can be segregated by different duration kinds
 * For each month/year... we take the latest that is contained in this date
 */
class TimeSeriesUC(private val assetPersistPort: AssetPersistPort) {

  fun monthlyTimeSeries(): List<AssetWithData> {
    return assetPersistPort.retrieveMonthlyTimeSeries()
  }

  fun dailyTimeSeries(startDateTime: LocalDateTime): List<AssetWithData> {
    return assetPersistPort.retrieveDailyTimeSeries(startDateTime.toLocalDate())
  }

  fun yearlyTimeSeries(): List<AssetWithData> {
    return assetPersistPort.retrieveYearlyTimeSeries()
  }

}
