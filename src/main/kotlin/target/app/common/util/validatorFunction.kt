package target.app.common.util

private val EMAIL_REGEX = Regex(
  pattern = "^(?![.])(?!.*[.]{2})[A-Z0-9._%+-]+(?<![.])@" +
      "(?:(?!-)[A-Z0-9-]{1,63}(?<!-)\\.)+[A-Z]{2,}$",
  option = RegexOption.IGNORE_CASE
)

fun isValidEmail(email: String): Boolean =
  !email.isBlank() && EMAIL_REGEX.matches(email)