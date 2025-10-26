package target.app.auth.port

interface EncryptPort {
  fun encrypt(value: String): String
  fun compare(value: String, encryptedValue: String): Boolean
}