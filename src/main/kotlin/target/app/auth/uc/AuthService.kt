package target.app.auth.uc

import target.app.auth.model.AuthException
import target.app.auth.port.CredentialPersistencePort
import target.app.auth.port.EncryptPort
import java.util.UUID

class AuthService(private val credentialPersistencePort: CredentialPersistencePort,
  private val encryptPort: EncryptPort
) {
  fun attemptLogin(userName: String, inputPassword: String): UUID {
    val credential = credentialPersistencePort.retrieveCredentialByUsername(userName) ?: throw  AuthException("credentials not found")
    if (!encryptPort.compare(inputPassword, credential.password)) throw AuthException("invalid credentials")
    return credential.userId
  }


}