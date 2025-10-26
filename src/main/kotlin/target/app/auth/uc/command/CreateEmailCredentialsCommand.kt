package target.app.auth.uc.command

import target.app.common.util.isValidEmail

data class CreateEmailCredentialsCommand(
  val email: String,
) {
  init {
    check(isValidEmail(email))
  }
}