package target.app.asset.web.manage

import kotlinx.serialization.Serializable
import target.app.asset.domain.model.AssetCategory
import target.app.asset.domain.model.AssetType
import target.app.common.Validatable
import target.app.common.ValidationException
import target.app.common.Validators

@Serializable
data class CreateAssetDTO(
  val title: String,
  val type: AssetType,
  val category: AssetCategory,
  val currency: String
) : Validatable {
  override fun validate() {
    Validators.requireNotBlank(title, "title")
    Validators.validateLength(title, "title", min = 1, max = 100)
    Validators.requireNotBlank(currency, "currency")

    // Validate currency code format (ISO 4217: 3 uppercase letters)
    if (!currency.matches(Regex("^[A-Z]{3}$"))) {
      throw ValidationException.fieldError("currency", "Currency must be a valid 3-letter ISO code (e.g., USD, EUR)")
    }
  }
}
