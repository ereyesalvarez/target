package target.app.auth.model

import java.util.UUID

data class Credential(
  val id: UUID = UUID.randomUUID(),
  val userId: UUID,
  val email: String,
  val username: String,
  val password: String
)
