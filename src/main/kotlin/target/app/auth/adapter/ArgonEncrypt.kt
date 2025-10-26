package target.app.auth.adapter

import target.app.auth.port.EncryptPort
import de.mkammerer.argon2.Argon2Factory


class ArgonEncrypt : EncryptPort {
  private val encoder = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id)
  override fun encrypt(value: String): String {
    return encoder.hash(4, 1024 * 1024, 8, value.toCharArray(), Charsets.UTF_8)
  }

  override fun compare(value: String, encryptedValue: String): Boolean {
    return encoder.verify(encryptedValue, value.toCharArray())
  }
}