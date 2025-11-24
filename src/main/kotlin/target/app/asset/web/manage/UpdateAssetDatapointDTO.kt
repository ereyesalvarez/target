package target.app.asset.web.manage

import kotlinx.serialization.Serializable
import target.infra.serializer.BigDecimalSerializer
import target.infra.serializer.LocalDateSerializer
import java.math.BigDecimal
import java.time.LocalDate


@Serializable
data class UpdateAssetDatapointDTO(
  val title: String,
  val dataPoints: List<UpdateDatapointItemDTO>,

){
  @Serializable
  data class UpdateDatapointItemDTO(
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate? = null,
    @Serializable(with = BigDecimalSerializer::class)
    val balance: BigDecimal,
    @Serializable(with = BigDecimalSerializer::class)
    val gain: BigDecimal?,
    @Serializable(with = BigDecimalSerializer::class)
    val contribution: BigDecimal?,
  )
}
