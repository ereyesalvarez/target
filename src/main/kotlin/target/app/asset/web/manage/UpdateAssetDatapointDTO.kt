package target.app.asset.web.manage

import kotlinx.serialization.Serializable
import target.app.common.Validatable
import target.app.common.ValidationException
import target.app.common.Validators
import target.infra.serializer.BigDecimalSerializer
import target.infra.serializer.LocalDateSerializer
import java.math.BigDecimal
import java.time.LocalDate


@Serializable
data class UpdateAssetDatapointDTO(
  val title: String,
  val dataPoints: List<UpdateDatapointItemDTO>,

) : Validatable {
  override fun validate() {
    Validators.requireNotBlank(title, "title")
    Validators.validateLength(title, "title", min = 1, max = 100)

    if (dataPoints.isEmpty()) {
      throw ValidationException.fieldError("dataPoints", "At least one data point is required")
    }

    dataPoints.forEachIndexed { index, dataPoint ->
      try {
        dataPoint.validate()
      } catch (e: ValidationException) {
        val message = e.message ?: "Invalid data point"
        throw ValidationException.fieldError("dataPoints[$index]", message)
      }
    }
  }

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
  ) : Validatable {
    override fun validate() {
      // No need of validation yet
    }
  }
}
