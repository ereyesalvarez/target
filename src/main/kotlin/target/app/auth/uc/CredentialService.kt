package target.app.auth.uc

import target.app.auth.model.Credential
import target.app.auth.uc.command.CreateEmailCredentialsCommand
import target.app.auth.uc.command.CreateUserPassCredentialsCommand

class CredentialService {


  /**
   * It generates the user (if it didn't exist)
   * If the email is present, will create a credential with email with this flow.
   * If not, it will generate a username and password based on the tenant
   */
  fun createCredentials(command: CreateEmailCredentialsCommand){
    // ToDo: Validate email (already existing user...)
    // TODO: If already exist we should launch an exception
    // ToDo: Create user and sent the notification so they can create the password.
    TODO("Not supported yet")
  }

  /**
   * Generate a
   */
  fun createCredentials(command: CreateUserPassCredentialsCommand): Credential {
    // ToDo: validate username
    // generate Password and return it
    // It should return the credentials for the recently created user
    TODO("Implement")
  }
}