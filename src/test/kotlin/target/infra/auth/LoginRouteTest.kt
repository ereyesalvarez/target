package target.infra.auth

import kotlinx.serialization.json.Json
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import target.infra.http.dto.ErrorResponse

class LoginRouteTest {

  private lateinit var jwtService: JwtService
  private lateinit var route: org.http4k.routing.RoutingHttpHandler

  @BeforeEach
  fun setup() {
    jwtService = JwtService(issuer = "test-app")
    route = loginRoute(jwtService)
    // Reset credentials for each test
    mvpUsername = "admin"
    mvpPassword = "admin123"
  }

  @Test
  fun `should return token on successful login`() {
    val request = Request(Method.POST, "/login")
      .header("Content-Type", "application/json")
      .body("""{"username":"admin","password":"admin123"}""")

    val response = route(request)

    assertEquals(Status.OK, response.status)
    assertEquals("application/json", response.header("Content-Type"))

    val loginResponse = Json.decodeFromString(LoginResponse.serializer(), response.bodyString())
    assertNotNull(loginResponse.token)
    assertTrue(loginResponse.token.isNotEmpty())
  }

  @Test
  fun `should return 401 for invalid username`() {
    val request = Request(Method.POST, "/login")
      .header("Content-Type", "application/json")
      .body("""{"username":"wronguser","password":"admin123"}""")

    val response = route(request)

    assertEquals(Status.UNAUTHORIZED, response.status)
    assertEquals("application/json", response.header("Content-Type"))

    val errorResponse = Json.decodeFromString(ErrorResponse.serializer(), response.bodyString())
    assertEquals("Invalid username or password", errorResponse.error)
  }

  @Test
  fun `should return 401 for invalid password`() {
    val request = Request(Method.POST, "/login")
      .header("Content-Type", "application/json")
      .body("""{"username":"admin","password":"wrongpassword"}""")

    val response = route(request)

    assertEquals(Status.UNAUTHORIZED, response.status)
    assertEquals("application/json", response.header("Content-Type"))

    val errorResponse = Json.decodeFromString(ErrorResponse.serializer(), response.bodyString())
    assertEquals("Invalid username or password", errorResponse.error)
  }

  @Test
  fun `should return 400 for invalid JSON format`() {
    val request = Request(Method.POST, "/login")
      .header("Content-Type", "application/json")
      .body("""{"invalid json}""")

    val response = route(request)

    assertEquals(Status.BAD_REQUEST, response.status)
    assertEquals("application/json", response.header("Content-Type"))

    val errorResponse = Json.decodeFromString(ErrorResponse.serializer(), response.bodyString())
    assertEquals("Invalid request format", errorResponse.error)
  }

  @Test
  fun `should return 400 for missing fields`() {
    val request = Request(Method.POST, "/login")
      .header("Content-Type", "application/json")
      .body("""{"username":"admin"}""")

    val response = route(request)

    assertEquals(Status.BAD_REQUEST, response.status)
  }

  @Test
  fun `generated token should be valid and contain username as subject`() {
    val request = Request(Method.POST, "/login")
      .header("Content-Type", "application/json")
      .body("""{"username":"admin","password":"admin123"}""")

    val response = route(request)
    val loginResponse = Json.decodeFromString(LoginResponse.serializer(), response.bodyString())

    // Verify token can be parsed and contains correct subject
    val token = loginResponse.token
    val subject = jwtService.getSubjectFromToken(token)
    assertEquals("admin", subject)

    val roles = jwtService.getRolesFromToken(token)
    assertEquals(listOf("ADMIN"), roles)
  }
}
