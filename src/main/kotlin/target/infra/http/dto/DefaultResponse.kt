package target.infra.http.dto

import kotlinx.serialization.Serializable

@Serializable
data class DefaultResponse (
  val message: String = ""
)
