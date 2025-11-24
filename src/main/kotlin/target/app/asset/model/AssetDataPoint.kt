package target.app.asset.model

import java.math.BigDecimal
import java.time.LocalDate

data class AssetDataPoint (
    val date: LocalDate,
    val balance: BigDecimal,
    val gain: BigDecimal?,
    val contribution: BigDecimal?
)
