package target.fixtures

import target.app.asset.domain.command.CreateAssetCommand
import target.app.asset.domain.model.AssetCategory
import target.app.asset.domain.model.AssetDataPoint
import target.app.asset.domain.model.AssetType
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Currency

val dpA1 = AssetDataPoint(
  date = LocalDate.of(2024, 1, 14),
  balance = BigDecimal("100.00"),
  gain = BigDecimal("50.00"),
  contribution = BigDecimal("50.00")
)
val dpA2 = AssetDataPoint(
  date = LocalDate.of(2024, 1, 15),
  balance = BigDecimal("110.00"),
  gain = BigDecimal("50.00"),
  contribution = BigDecimal("60.00")
)
val dpA3 = AssetDataPoint(
  date = LocalDate.of(2024, 2, 15),
  balance = BigDecimal("0.00"),
  gain = BigDecimal("50.00"),
  contribution = BigDecimal("0.00")
)
val dpA4 = AssetDataPoint(
  date = LocalDate.of(2024, 2, 15),
  balance = BigDecimal("120.00"),
  gain = BigDecimal("60.00"),
  contribution = BigDecimal("60.00")
)
val dpA5 = AssetDataPoint(
  date = LocalDate.of(2025, 2, 15),
  balance = BigDecimal("130.00"),
  gain = BigDecimal("60.00"),
  contribution = BigDecimal("70.00")
)

val dpA6 = AssetDataPoint(
  date = LocalDate.of(2025, 2, 16),
  balance = BigDecimal("500.00"),
  gain = BigDecimal("0.00"),
  contribution = null
)

val dpB1 = AssetDataPoint(
  date = LocalDate.of(2024, 1, 1),
  balance = BigDecimal("100.00"),
  gain = null,
  contribution = null
)
val dpB2 = AssetDataPoint(
  date = LocalDate.of(2024, 1, 2),
  balance = BigDecimal("200.00"),
  gain = null,
  contribution = null
)
val dpB3 = AssetDataPoint(
  date = LocalDate.of(2024, 1, 4),
  balance = BigDecimal("300.00"),
  gain = null,
  contribution = null
)
val dpB4 = AssetDataPoint(
  date = LocalDate.of(2024, 1, 6),
  balance = BigDecimal("200.00"),
  gain = null,
  contribution = null
)

val assetA = CreateAssetCommand(
  title = "Investment Account",
  type = AssetType.STOCKS,
  category = AssetCategory.Investment,
  currency = Currency.getInstance("USD")
)

val assetB = CreateAssetCommand(
  title = "Revolut Account",
  type = AssetType.CHECKING,
  category = AssetCategory.Cash,
  currency = Currency.getInstance("USD")
)

val assetC = CreateAssetCommand(
  title = "Old account",
  type = AssetType.SAVINGS,
  category = AssetCategory.Cash,
  currency = Currency.getInstance("USD")
)
