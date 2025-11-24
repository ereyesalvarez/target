package target.app.asset.adapter.out

import org.jdbi.v3.core.Jdbi
import target.app.asset.domain.command.CreateAssetCommand
import target.app.asset.domain.model.AssetCategory
import target.app.asset.domain.model.AssetDataPoint
import target.app.asset.domain.model.AssetType
import target.app.asset.domain.model.AssetWithData
import target.app.asset.domain.port.out.AssetPersistItem
import target.app.asset.domain.port.out.AssetPersistPort
import java.time.LocalDate
import java.util.Currency

class AssetDBAdapter(private val jdbi: Jdbi) : AssetPersistPort {

  override fun createAsset(command: CreateAssetCommand) {
    jdbi.useHandle<Exception> { handle ->
      handle.createUpdate(
        """
                INSERT INTO asset (title, type_code, category_code, currency)
                VALUES (:title, :type, :category, :currency)
                """
      )
        .bind("title", command.title)
        .bind("type", command.type.name)       // enum → String
        .bind("category", command.category.name)
        .bind("currency", command.currency.currencyCode)
        .execute()
    }
  }

  override fun checkIfExists(title: String): Boolean {
    return jdbi.withHandle<Boolean, Exception> { handle ->
      handle.createQuery("SELECT 1 FROM asset WHERE title = :title LIMIT 1")
        .bind("title", title)
        .mapTo(Int::class.java)
        .findOne()
        .isPresent
    }
  }

  override fun upsertAssetStatus(
    title: String,
    datapoint: AssetDataPoint
  ) {
        jdbi.useHandle<Exception> { handle ->
            // First, find the asset ID by title
            val assetId = handle.createQuery("SELECT id FROM asset WHERE title = :title")
                .bind("title", title)
                .mapTo(Long::class.java)
                .findFirst()
                .orElseThrow { IllegalArgumentException("Asset with title '${title}' not found") }

            // Upsert the asset status
            val upsertQuery = """
        INSERT INTO asset_datapoint (asset_id, d, balance, contribution, gain) 
        VALUES (:assetId, :date, :balance, :contribution, :gain)
        ON CONFLICT (asset_id, d) 
        DO UPDATE SET 
          balance = EXCLUDED.balance,
          contribution = EXCLUDED.contribution,
          gain = EXCLUDED.gain
        WHERE asset_datapoint.balance IS DISTINCT FROM EXCLUDED.balance
           OR asset_datapoint.contribution IS DISTINCT FROM EXCLUDED.contribution
           OR asset_datapoint.gain IS DISTINCT FROM EXCLUDED.gain;
      """

            handle.createUpdate(upsertQuery)
                .bind("assetId", assetId)
                .bind("date", datapoint.date)
                .bind("balance", datapoint.balance)
                .bind("contribution", datapoint.contribution)
                .bind("gain", datapoint.gain)
                .execute()
        }
    }

  override fun retrieveAssets(): List<AssetPersistItem> {
    return jdbi.withHandle<List<AssetPersistItem>, Exception> { handle ->
      handle.createQuery(
        """
        SELECT id, title, type_code, category_code, currency, archived, archived_at
        FROM asset
        ORDER BY title
        """
      )
        .map { rs, _ ->
          AssetPersistItem(
            id = rs.getInt("id"),
            title = rs.getString("title"),
            type = AssetType.valueOf(rs.getString("type_code")),
            categoryCode = AssetCategory.valueOf(rs.getString("category_code")),
            currency = Currency.getInstance(rs.getString("currency")),
            archived = rs.getBoolean("archived"),
            archivedAt = rs.getString("archived_at")
          )
        }
        .list()
    }
  }

  override fun retrieveMonthlyTimeSeries(): List<AssetWithData> {
    return getAssetsWithTimeSeries("monthly")
  }

  override fun retrieveDailyTimeSeries(startDate: LocalDate): List<AssetWithData> {
    return getAssetsWithTimeSeries("daily", startDate)
  }

  override fun retrieveYearlyTimeSeries(): List<AssetWithData> {
    return getAssetsWithTimeSeries("yearly")
  }

  private fun getAssetsWithTimeSeries(period: String, startDate: LocalDate? = null): List<AssetWithData> {
    return jdbi.withHandle<List<AssetWithData>, Exception> { handle ->
      val assets = retrieveAssets()
      
      assets.map { asset ->
        val dataPoints = when (period) {
          "daily" -> {
            if (startDate != null) {
              handle.createQuery(
                """
                SELECT d, balance, gain, contribution
                FROM asset_datapoint
                WHERE asset_id = :assetId AND d >= :startDate
                ORDER BY d
                """
              )
                .bind("assetId", asset.id)
                .bind("startDate", startDate)
                .map { rs, _ ->
                  AssetDataPoint(
                    date = rs.getDate("d").toLocalDate(),
                    balance = rs.getBigDecimal("balance"),
                    gain = rs.getBigDecimal("gain"),
                    contribution = rs.getBigDecimal("contribution")
                  )
                }
                .list()
            } else emptyList()
          }
          "monthly" -> {
            handle.createQuery(
              """
              SELECT DISTINCT ON (DATE_TRUNC('month', d)) d, balance, gain, contribution
              FROM asset_datapoint
              WHERE asset_id = :assetId
              ORDER BY DATE_TRUNC('month', d), d DESC
              """
            )
              .bind("assetId", asset.id)
              .map { rs, _ ->
                AssetDataPoint(
                  date = rs.getDate("d").toLocalDate(),
                  balance = rs.getBigDecimal("balance"),
                  gain = rs.getBigDecimal("gain"),
                  contribution = rs.getBigDecimal("contribution")
                )
              }
              .list()
          }
          "yearly" -> {
            handle.createQuery(
              """
              SELECT DISTINCT ON (DATE_TRUNC('year', d)) d, balance, gain, contribution
              FROM asset_datapoint
              WHERE asset_id = :assetId
              ORDER BY DATE_TRUNC('year', d), d DESC
              """
            )
              .bind("assetId", asset.id)
              .map { rs, _ ->
                AssetDataPoint(
                  date = rs.getDate("d").toLocalDate(),
                  balance = rs.getBigDecimal("balance"),
                  gain = rs.getBigDecimal("gain"),
                  contribution = rs.getBigDecimal("contribution")
                )
              }
              .list()
          }
          else -> emptyList()
        }
        
        AssetWithData(
          title = asset.title,
          type = asset.type,
          category = asset.categoryCode,
          currency = asset.currency,
          items = dataPoints
        )
      }
    }
  }
}
