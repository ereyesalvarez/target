package target.app.auth.port

import target.app.auth.model.Credential

interface CredentialPersistencePort {
  fun persistCredentials(credential: Credential)

  fun retrieveCredentialByEmail(email: String): Credential?

  fun retrieveCredentialByUsername(username: String): Credential?
}