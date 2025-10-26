package target.infra.http

import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.junit.jupiter.api.Test
import target.infra.http.routes.healthHandler
import kotlin.test.assertEquals

class BaseHandlersTest {

  @Test
  fun `Ping endpoint return pong`() {
    assertEquals(
        Response.Companion(Status.Companion.OK).body("pong"),
      healthHandler(Request.Companion(Method.GET, "/ping"))
    )
  }
}
