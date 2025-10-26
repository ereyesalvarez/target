package target.it

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.slf4j.LoggerFactory
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.containers.wait.strategy.Wait
import target.App
import target.infra.auth.LoginResponse
import target.infra.http.dto.DefaultResponse
import target.infra.http.dto.ErrorResponse
import target.infra.properties.definition.AppDBConfig

@Tag("integration")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TargetAppIT {
  private val log = LoggerFactory.getLogger(TargetAppIT::class.java)

  private var postgres: PostgreSQLContainer<*> =
    PostgreSQLContainer("postgres:17").withInitScript("schema.sql").withReuse(true)
      .waitingFor(Wait.forLogMessage(".*database system is ready to accept connections.*", 2))
      .waitingFor(Wait.forListeningPort())
  private lateinit var app: App
  val client = OkHttpClient()
  private val json = "application/json; charset=utf-8".toMediaType()
  private lateinit var jdbi: Jdbi
  private lateinit var baseUrl: String
  private val postgresNeeded = true
  @BeforeAll
  fun setupAll() {
    if (postgresNeeded) {
      postgres.start()
      AppDBConfig(postgres.jdbcUrl, postgres.username, postgres.password)
      jdbi = Jdbi.create(postgres.jdbcUrl, postgres.username, postgres.password)
      System.setProperty("config.override.db.jdbc", postgres.jdbcUrl)
      System.setProperty("config.override.db.username", postgres.username)
      System.setProperty("config.override.db.password", postgres.password)
    }
    // wait
    app = App()
    val server = app.start()
    baseUrl = "http://localhost:" + server.port()
  }

  @AfterAll
  fun tearDown() {
    postgres.stop()
  }

  @BeforeEach
  fun setup() {
    // Database connection check removed - not needed for auth tests
  }


  @Test
  fun `E2E test for successful login with valid credentials`() {
    log.info("Testing successful login flow")

    val loginRequestBody = """{"username":"admin","password":"admin123"}"""
    val requestBody = loginRequestBody.toRequestBody(json)

    val request = Request.Builder()
      .url("$baseUrl/login")
      .post(requestBody)
      .build()

    client.newCall(request).execute().use { response ->
      assertEquals(200, response.code, "Expected 200 OK for valid credentials")
      assertEquals("application/json", response.header("Content-Type"))

      val responseBody = response.body?.string()
      assertNotNull(responseBody, "Response body should not be null")

      val loginResponse = Json.decodeFromString(LoginResponse.serializer(), responseBody!!)
      assertNotNull(loginResponse.token, "Token should not be null")
      assertTrue(loginResponse.token.isNotEmpty(), "Token should not be empty")
      assertTrue(loginResponse.token.startsWith("eyJ"), "Token should be a valid JWT format")

      log.info("Successfully received JWT token")
    }
  }

  @Test
  fun `E2E test for login with invalid username`() {
    log.info("Testing login with invalid username")

    val loginRequestBody = """{"username":"wronguser","password":"admin123"}"""
    val requestBody = loginRequestBody.toRequestBody(json)

    val request = Request.Builder()
      .url("$baseUrl/login")
      .post(requestBody)
      .build()

    client.newCall(request).execute().use { response ->
      assertEquals(401, response.code, "Expected 401 Unauthorized for invalid username")
      assertEquals("application/json", response.header("Content-Type"))

      val responseBody = response.body?.string()
      assertNotNull(responseBody)

      val errorResponse = Json.decodeFromString(ErrorResponse.serializer(), responseBody!!)
      assertEquals("Invalid username or password", errorResponse.error)

      log.info("Correctly rejected invalid username")
    }
  }

  @Test
  fun `E2E test for login with invalid password`() {
    log.info("Testing login with invalid password")

    val loginRequestBody = """{"username":"admin","password":"wrongpassword"}"""
    val requestBody = loginRequestBody.toRequestBody(json)

    val request = Request.Builder()
      .url("$baseUrl/login")
      .post(requestBody)
      .build()

    client.newCall(request).execute().use { response ->
      assertEquals(401, response.code, "Expected 401 Unauthorized for invalid password")
      assertEquals("application/json", response.header("Content-Type"))

      val responseBody = response.body?.string()
      assertNotNull(responseBody)

      val errorResponse = Json.decodeFromString(ErrorResponse.serializer(), responseBody!!)
      assertEquals("Invalid username or password", errorResponse.error)

      log.info("Correctly rejected invalid password")
    }
  }

  @Test
  fun `E2E test for login with malformed JSON`() {
    log.info("Testing login with malformed JSON")

    val loginRequestBody = """{"invalid json}"""
    val requestBody = loginRequestBody.toRequestBody(json)

    val request = Request.Builder()
      .url("$baseUrl/login")
      .post(requestBody)
      .build()

    client.newCall(request).execute().use { response ->
      assertEquals(400, response.code, "Expected 400 Bad Request for malformed JSON")
      assertEquals("application/json", response.header("Content-Type"))

       val responseBody = response.body?.string()
      assertNotNull(responseBody)

      val errorResponse = Json.decodeFromString(ErrorResponse.serializer(), responseBody!!)
      assertEquals("Invalid request format", errorResponse.error)

      log.info("Correctly rejected malformed JSON")
    }
  }

  @Test
  fun `E2E test for complete authentication flow - login and verify token`() {
    log.info("Testing complete authentication flow")

    // Step 1: Login with valid credentials
    val loginRequestBody = """{"username":"admin","password":"admin123"}"""
    val requestBody = loginRequestBody.toRequestBody(json)

    val loginRequest = Request.Builder()
      .url("$baseUrl/login")
      .post(requestBody)
      .build()

    val token = client.newCall(loginRequest).execute().use { response ->
      assertEquals(200, response.code, "Login should succeed")

      val responseBody = response.body?.string()
      val loginResponse = Json.decodeFromString(LoginResponse.serializer(), responseBody!!)
      loginResponse.token
    }

    log.info("Step 1: Successfully logged in and received token")

    // Step 2: Verify token is in valid JWT format (3 parts separated by dots)
    val tokenParts = token.split(".")
    assertEquals(3, tokenParts.size, "JWT should have 3 parts (header.payload.signature)")
    assertTrue(tokenParts.all { it.isNotEmpty() }, "All JWT parts should be non-empty")

    log.info("Step 2: Token format validated successfully")
    log.info("Complete authentication flow test passed")
  }

  @Test
  fun `authenticated route returns caller username`() {
    val token = loginAndGetToken()

    val request = Request.Builder()
      .url("$baseUrl/me")
      .get()
      .addHeader("Authorization", "Bearer $token")
      .build()

    client.newCall(request).execute().use { response ->
      assertEquals(200, response.code, "Expected 200 OK for authenticated /me route")
      assertEquals("application/json", response.header("Content-Type"))

      val responseBody = response.body?.string()
      assertNotNull(responseBody, "Response body should not be null")

      val defaultResponse = Json.decodeFromString(DefaultResponse.serializer(), responseBody!!)
      assertEquals("Authenticated as admin", defaultResponse.message)
    }
  }

  private fun loginAndGetToken(username: String = "admin", password: String = "admin123"): String {
    val loginRequestBody = """{"username":"$username","password":"$password"}"""
    val requestBody = loginRequestBody.toRequestBody(json)

    val loginRequest = Request.Builder()
      .url("$baseUrl/login")
      .post(requestBody)
      .build()

    return client.newCall(loginRequest).execute().use { response ->
      assertEquals(200, response.code, "Login should succeed to call authenticated routes")

      val responseBody = response.body?.string()
      assertNotNull(responseBody, "Login response body should not be null")

      val loginResponse = Json.decodeFromString(LoginResponse.serializer(), responseBody!!)
      loginResponse.token
    }
  }
}
